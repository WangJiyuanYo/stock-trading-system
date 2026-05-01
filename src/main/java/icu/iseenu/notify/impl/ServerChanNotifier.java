package icu.iseenu.notify.impl;

import icu.iseenu.notify.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service("serverChanNotifier")
public class ServerChanNotifier implements NotificationSender {
    private static final Logger log = LoggerFactory.getLogger(ServerChanNotifier.class);
    
    private static final String SERVER_CHAN_URL = "https://sctapi.ftqq.com/";

    @Value("${notification.serverchan.sckey}")
    private String sendKey;

    private final WebClient webClient;

    public ServerChanNotifier(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(SERVER_CHAN_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Override
    public void send(String title, String message) {
        if (sendKey == null || sendKey.trim().isEmpty()) {
            log.warn("Server 酱 SendKey 未配置，无法发送消息");
            return;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("title", title);
            params.put("desp", message != null ? message : "");

            // 正确的 URL 格式：https://sctapi.ftqq.com/{sendkey}.send
            String response = webClient.post()
                    .uri(sendKey + ".send")
                    .bodyValue(buildFormData(params))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Server 酱推送响应：{}", response);
            
            // 检查响应是否成功
            boolean success = response != null && (response.contains("\"code\":0") || response.contains("\"errno\":0"));
            if (!success) {
                log.warn("Server 酱推送可能失败，响应：{}", response);
            }
            
        } catch (Exception e) {
            log.error("Server 酱推送失败：{}", e.getMessage(), e);
        }
    }

    @Override
    public String name() {
        return "serverchan";
    }

    /**
     * 构建表单数据
     */
    private String buildFormData(Map<String, Object> params) {
        StringBuilder formData = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (formData.length() > 0) {
                formData.append("&");
            }
            try {
                String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8);
                formData.append(key).append("=").append(value);
            } catch (Exception e) {
                log.error("URL 编码失败：{}", e.getMessage());
            }
        }
        
        return formData.toString();
    }
}
