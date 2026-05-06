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
import java.util.UUID;

@Component
@Slf4j
public class FeishuMessageSender implements NotificationChannel {

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
        String enabledChannels = notificationProperties.getEnabledChannels();
        return enabledChannels != null && enabledChannels.contains(getName());
    }

    @Override
    public void send(String title, String message) {
        String webhookUrl = notificationProperties.getFeishu().getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("飞书 Webhook URL 未配置，无法发送广播通知");
            return;
        }

        String textContent = message.replace("# ", "").replace("## ", "").replace("|", "");

        StringBuilder cardBuilder = new StringBuilder();
        cardBuilder.append("{");
        cardBuilder.append("\"config\":{\"wide_screen_mode\":true},");
        cardBuilder.append("\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"");
        cardBuilder.append(escapeJson(title));
        cardBuilder.append("\"},\"template\":\"blue\"},");
        cardBuilder.append("\"elements\":[");
        cardBuilder.append("{\"tag\":\"div\",\"text\":{\"tag\":\"lark_md\",\"content\":\"");
        cardBuilder.append(escapeJson(textContent));
        cardBuilder.append("\"}}");
        cardBuilder.append("]}");

        try {
            String response = webClient.post()
                    .uri(webhookUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(cardBuilder.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("飞书 Webhook 通知发送成功: {}", response);
        } catch (Exception e) {
            log.error("飞书 Webhook 通知发送失败", e);
        }
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

        File tempFile = null;
        try {
            String extension = imageUrl.contains(".png") ? ".png" : ".jpg";
            Path tempPath = Files.createTempFile("feishu_img_", extension);
            tempFile = tempPath.toFile();
            try (InputStream in = new URL(imageUrl).openStream()) {
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
                log.error("上传图片到飞书失败 - code:{}, msg:{}",
                        imageResp.getCode(), imageResp.getMsg());
                return;
            }

            String imageKey = imageResp.getData().getImageKey();
            log.info("图片上传飞书成功: imageKey={}", imageKey);

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
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
