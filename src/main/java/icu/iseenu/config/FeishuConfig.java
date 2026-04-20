package icu.iseenu.config;

import com.lark.oapi.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FeishuConfig {

    @Value("${feishu.app-id}")
    private String appId;

    @Value("${feishu.app-secret}")
    private String appSecret;

    /**
     * 创建 HTTP Client，用于发送消息等 API 调用
     */
    @Bean
    public Client feishuHttpClient() {

        return new Client.Builder(appId, appSecret)
                .build();
    }
}
