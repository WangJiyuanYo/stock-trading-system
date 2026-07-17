package icu.iseenu.notification.feishu;

import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.im.v1.model.CreateImageReq;
import com.lark.oapi.service.im.v1.model.CreateImageReqBody;
import com.lark.oapi.service.im.v1.model.CreateImageResp;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import icu.iseenu.infra.config.NotificationProperties;
import icu.iseenu.notification.channel.NotificationChannel;
import icu.iseenu.notification.NotificationDeliveryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Duration;

@Component
@Slf4j
public class FeishuMessageSender implements NotificationChannel {

    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile(
            "!\\[([^\\]]*)]\\((https?://[^\\s)]+)\\)");

    private final NotificationProperties notificationProperties;
    private final Client client;
    private final WebClient webClient;

    public FeishuMessageSender(NotificationProperties notificationProperties,
                               @Autowired(required = false) Client client,
                               WebClient.Builder webClientBuilder) {
        this.notificationProperties = notificationProperties;
        this.client = client;
        this.webClient = webClientBuilder.build();
    }

    // ── NotificationChannel 接口（Webhook 广播） ──

    @Override
    public String getName() {
        return "feishu";
    }

    @Override
    public boolean isEnabled() {
        return notificationProperties.isChannelEnabled(getName());
    }

    @Override
    public boolean supportsScene(String scene) {
        List<String> enabledScenes = notificationProperties.getFeishu().getEnabledScenes();
        if (enabledScenes == null || enabledScenes.isEmpty()) {
            return true;
        }
        String normalizedScene = scene == null ? "default" : scene.trim().toLowerCase(Locale.ROOT);
        return enabledScenes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .anyMatch(value -> "*".equals(value) || value.equals(normalizedScene));
    }

    @Override
    public void send(String title, String message) {
        sendWithResult("default", title, message);
    }

    @Override
    public NotificationDeliveryResult sendWithResult(String scene, String title, String message) {
        String webhookUrl = notificationProperties.getFeishu().getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("飞书 Webhook URL 未配置，无法发送广播通知");
            return NotificationDeliveryResult.failure(getName(), "FEISHU_WEBHOOK_URL is not configured");
        }

        String payload = buildWebhookPayload(title, removeMarkdownImages(message));

        try {
            String response = postWebhook(webhookUrl, payload);
            assertWebhookSucceeded(response);
            log.info("飞书 Webhook 通知发送成功: {}", response);

            for (ImageReference image : extractMarkdownImages(message)) {
                sendWebhookImage(webhookUrl, image);
            }
            return NotificationDeliveryResult.success(getName());
        } catch (Exception e) {
            log.error("飞书 Webhook 通知发送失败", e);
            return NotificationDeliveryResult.failure(getName(), e.getMessage());
        }
    }

    private String postWebhook(String webhookUrl, String payload) {
        return webClient.post()
                .uri(webhookUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(15));
    }

    private void assertWebhookSucceeded(String response) {
        try {
            int code = JsonParser.parseString(response).getAsJsonObject().get("code").getAsInt();
            if (code != 0) {
                throw new IllegalStateException("Feishu webhook rejected message: " + response);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Invalid Feishu webhook response: " + response, e);
        }
    }

    private void sendWebhookImage(String webhookUrl, ImageReference image) {
        if (client == null) {
            sendWebhookImageLink(webhookUrl, image);
            return;
        }

        try {
            String imageKey = uploadImage(image.url());
            String response = postWebhook(webhookUrl, buildImageWebhookPayload(imageKey));
            assertWebhookSucceeded(response);
            log.info("飞书 Webhook 图片发送成功: {}", response);
        } catch (Exception e) {
            log.warn("飞书图片上传或发送失败，降级为图片链接: {}", e.getMessage());
            sendWebhookImageLink(webhookUrl, image);
        }
    }

    private void sendWebhookImageLink(String webhookUrl, ImageReference image) {
        try {
            String imageMessage = image.label() + "：" + image.url();
            String imageResponse = postWebhook(webhookUrl, buildTextWebhookPayload(imageMessage));
            assertWebhookSucceeded(imageResponse);
            log.info("飞书 Webhook 图片链接发送成功: {}", imageResponse);
        } catch (Exception e) {
            log.error("飞书 Webhook 图片链接发送失败", e);
        }
    }

    /**
     * Custom-bot webhooks require the outer msg_type and card fields. The SDK
     * message API below accepts the card object itself, so it intentionally does
     * not use this wrapper.
     */
    static String buildWebhookPayload(String title, String message) {
        String textContent = (message == null ? "" : message)
                .replace("# ", "").replace("## ", "").replace("|", "");

        StringBuilder cardBuilder = new StringBuilder();
        cardBuilder.append("{\"msg_type\":\"interactive\",\"card\":{");
        cardBuilder.append("\"config\":{\"wide_screen_mode\":true},");
        cardBuilder.append("\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"");
        cardBuilder.append(escapeJsonStatic(title));
        cardBuilder.append("\"},\"template\":\"blue\"},");
        cardBuilder.append("\"elements\":[");
        cardBuilder.append("{\"tag\":\"div\",\"text\":{\"tag\":\"lark_md\",\"content\":\"");
        cardBuilder.append(escapeJsonStatic(textContent));
        cardBuilder.append("\"}}]}}");
        return cardBuilder.toString();
    }

    static String buildTextWebhookPayload(String message) {
        return "{\"msg_type\":\"text\",\"content\":{\"text\":\""
                + escapeJsonStatic(message) + "\"}}";
    }

    static String buildImageWebhookPayload(String imageKey) {
        return "{\"msg_type\":\"image\",\"content\":{\"image_key\":\""
                + escapeJsonStatic(imageKey) + "\"}}";
    }

    static String removeMarkdownImages(String message) {
        if (message == null || message.isBlank()) {
            return message == null ? "" : message;
        }
        String withoutImages = MARKDOWN_IMAGE_PATTERN.matcher(message).replaceAll("");
        return withoutImages.replaceAll("\\n{3,}", "\\n\\n").trim();
    }

    static List<ImageReference> extractMarkdownImages(String message) {
        List<ImageReference> images = new ArrayList<>();
        if (message == null || message.isBlank()) {
            return images;
        }

        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(message);
        while (matcher.find()) {
            String label = matcher.group(1).isBlank() ? "图片详情" : matcher.group(1);
            images.add(new ImageReference(label, matcher.group(2)));
        }
        return images;
    }

    record ImageReference(String label, String url) {
    }

    // ── 定向发送（SDK API） ──

    public void sendMarkdownMessage(String senderId, String markdownContent) {
        if (client == null) {
            log.warn("飞书 Client 未初始化，无法发送定向消息");
            return;
        }

        String textContent = markdownContent.replace("# ", "").replace("## ", "").replace("|", "");

        StringBuilder cardBuilder = new StringBuilder();
        cardBuilder.append("{");
        cardBuilder.append("\"config\":{\"wide_screen_mode\":true},");
        cardBuilder.append("\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"AI 助手\"},\"template\":\"blue\"},");
        cardBuilder.append("\"elements\":[");
        cardBuilder.append("{\"tag\":\"div\",\"text\":{\"tag\":\"lark_md\",\"content\":\"");
        cardBuilder.append(escapeJson(textContent));
        cardBuilder.append("\"}}");
        cardBuilder.append("]}");

        String content = cardBuilder.toString();
        log.debug("发送卡片消息内容: {}", content);

        CreateMessageReq req = CreateMessageReq.newBuilder()
                .createMessageReqBody(CreateMessageReqBody.newBuilder()
                        .receiveId(senderId)
                        .msgType("interactive")
                        .content(content)
                        .uuid(UUID.randomUUID().toString())
                        .build())
                .receiveIdType("open_id")
                .build();

        try {
            CreateMessageResp resp = client.im().v1().message().create(req);

            if (!resp.success()) {
                log.error("发送卡片消息失败 - code:{},msg:{},reqId:{}, resp:{}",
                        resp.getCode(), resp.getMsg(), resp.getRequestId(),
                        Jsons.createGSON(true, false).toJson(JsonParser.parseString(
                                new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
                return;
            }

            log.info("卡片消息发送成功");
        } catch (Exception e) {
            log.error("发送卡片消息异常", e);
        }
    }

    public void sendImageMessage(String senderId, String imageUrl) {
        if (client == null) {
            log.warn("飞书 Client 未初始化，无法发送图片消息");
            return;
        }

        try {
            String imageKey = uploadImage(imageUrl);

            String content = "{\"image_key\":\"" + imageKey + "\"}";
            CreateMessageReq msgReq = CreateMessageReq.newBuilder()
                    .createMessageReqBody(CreateMessageReqBody.newBuilder()
                            .receiveId(senderId)
                            .msgType("image")
                            .content(content)
                            .uuid(UUID.randomUUID().toString())
                            .build())
                    .receiveIdType("open_id")
                    .build();

            CreateMessageResp msgResp = client.im().v1().message().create(msgReq);
            if (!msgResp.success()) {
                log.error("发送图片消息失败 - code:{}, msg:{}",
                        msgResp.getCode(), msgResp.getMsg());
            } else {
                log.info("图片消息发送成功");
            }
        } catch (Exception e) {
            log.error("发送图片消息异常", e);
        }
    }

    private String uploadImage(String imageUrl) throws Exception {
        String extension = imageUrl.contains(".png") ? ".png" : ".jpg";
        Path tempPath = Files.createTempFile("feishu_img_", extension);
        File tempFile = tempPath.toFile();
        try {
            java.net.URLConnection connection = new URL(imageUrl).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(20_000);
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, tempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("图片下载完成: {} -> {}", imageUrl, tempFile.getAbsolutePath());

            CreateImageReqBody imageBody = CreateImageReqBody.newBuilder()
                    .imageType("message")
                    .image(tempFile)
                    .build();
            CreateImageReq imageReq = CreateImageReq.newBuilder()
                    .createImageReqBody(imageBody)
                    .build();
            CreateImageResp imageResp = client.im().v1().image().create(imageReq);
            if (!imageResp.success()) {
                throw new IllegalStateException("上传图片到飞书失败 - code:" + imageResp.getCode()
                        + ", msg:" + imageResp.getMsg());
            }

            String imageKey = imageResp.getData().getImageKey();
            log.info("图片上传飞书成功: imageKey={}", imageKey);
            return imageKey;
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    private String escapeJson(String s) {
        return escapeJsonStatic(s);
    }

    private static String escapeJsonStatic(String value) {
        String safeValue = value == null ? "" : value;
        return safeValue.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
