package icu.iseenu.notification.channel;

import icu.iseenu.infra.config.NotificationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Duration;

/**
 * NotifyMe 通知渠道
 */
@Component
@Slf4j
public class NotifyMeChannel implements NotificationChannel {

    private final NotificationProperties notificationProperties;
    private final WebClient webClient;

    private static final String DEFAULT_GROUP = "STOCK";
    private static final boolean DEFAULT_BIG_TEXT = false;
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile(
            "!\\[([^\\]]*)]\\((https?://[^\\s)]+)\\)");

    public NotifyMeChannel(NotificationProperties notificationProperties, WebClient.Builder webClientBuilder) {
        this.notificationProperties = notificationProperties;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public void send(String title, String message) {
        send("default", title, message);
    }

    @Override
    public void send(String scene, String title, String message) {
        if (!isEnabled()) {
            return;
        }

        List<String> uuids = notificationProperties.getNotifyme().getResolvedUuids(scene);
        if (uuids.isEmpty()) {
            log.warn("NotifyMe 场景 {} 未配置有效接收人，无法发送消息", scene);
            return;
        }

        int successCount = 0;
        for (int i = 0; i < uuids.size(); i++) {
            if (sendToUuid(uuids.get(i), scene, title, message, i + 1, uuids.size())) {
                successCount++;
            }
        }

        if (successCount == uuids.size()) {
            log.info("NotifyMe 场景 {} 全部推送成功，共 {} 个接收端✅", scene, successCount);
        } else {
            log.warn("NotifyMe 场景 {} 推送完成：成功 {} 个，失败 {} 个",
                    scene, successCount, uuids.size() - successCount);
        }
    }

    private boolean sendToUuid(String uuid, String scene, String title, String message,
                               int current, int total) {
        try {
            String notifyMeMessage = formatMessage(message);
            // 发送 GET 请求，使用 UriBuilder 让 WebClient 自动处理编码
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("notifyme-server.wzn556.top")
                            .path("/")
                            .queryParam("uuid", uuid)
                            .queryParam("title", title != null ? title : "")
                            .queryParam("body", notifyMeMessage)
                            .queryParam("group", resolveGroup(scene))
                            .queryParam("bigText", DEFAULT_BIG_TEXT)
                            .build())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(15));

            // 检查响应是否成功
            if (response != null && response.contains("\"isSuccess\":true")) {
                log.info("NotifyMe 推送成功 [{}/{}]✅", current, total);
                return true;
            }

            log.warn("NotifyMe 推送失败 [{}/{}]，响应：{}", current, total, response);
        } catch (Exception e) {
            // 单个接收端失败不影响其余 UUID 继续发送
            log.error("NotifyMe 推送异常 [{}/{}]：{}", current, total, e.getMessage(), e);
        }
        return false;
    }

    /**
     * NotifyMe does not render Markdown images in the notification body. Convert
     * them to a readable label followed by the original URL, while leaving the
     * source message unchanged for channels that support richer content.
     */
    static String formatMessage(String message) {
        if (message == null || message.isBlank()) {
            return message == null ? "" : message;
        }

        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String label = matcher.group(1).isBlank() ? "图片" : matcher.group(1);
            String replacement = label + "：" + matcher.group(2);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String resolveGroup(String scene) {
        if (scene == null || scene.isBlank() || "default".equalsIgnoreCase(scene)) {
            return DEFAULT_GROUP;
        }
        return scene.trim().toUpperCase();
    }

    @Override
    public String getName() {
        return "notifyme";
    }
    
    @Override
    public boolean isEnabled() {
        return notificationProperties.isChannelEnabled(getName());
    }


}
