package icu.iseenu.notification.channel;

import icu.iseenu.infra.config.NotificationProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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

    public NotifyMeChannel(NotificationProperties notificationProperties, WebClient.Builder webClientBuilder) {
        this.notificationProperties = notificationProperties;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public void send(String title, String message) {
        if (!isEnabled()) {
            return;
        }
        
        String uuid = notificationProperties.getNotifyme().getUuid();
        if (uuid == null || uuid.trim().isEmpty()) {
            log.warn("NotifyMe UUID 未配置，无法发送消息");
            return;
        }

        try {
            // 发送 GET 请求，使用 UriBuilder 让 WebClient 自动处理编码
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("notifyme-server.wzn556.top")
                            .path("/")
                            .queryParam("uuid", uuid)
                            .queryParam("title", title != null ? title : "")
                            .queryParam("body", message != null ? message : "")
                            .queryParam("group", DEFAULT_GROUP)
                            .queryParam("bigText", DEFAULT_BIG_TEXT)
                            .build())
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("NotifyMe 推送响应：{}", response);
            
            // 检查响应是否成功
            if (response != null && response.contains("\"isSuccess\":true")) {
                log.info("NotifyMe 推送成功✅");
            } else {
                log.warn("NotifyMe 推送可能失败，响应：{}", response);
            }
            
        } catch (Exception e) {
            log.error("NotifyMe 推送失败：{}", e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "notifyme";
    }
    
    @Override
    public boolean isEnabled() {
        String enabledChannels = notificationProperties.getEnabledChannels();
        return enabledChannels != null && enabledChannels.contains(getName());
    }


}
