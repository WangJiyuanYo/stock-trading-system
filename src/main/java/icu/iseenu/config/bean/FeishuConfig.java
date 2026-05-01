package icu.iseenu.config.bean;

import com.lark.oapi.Client;
import icu.iseenu.config.properties.FeishuProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 飞书配置
 * 创建飞书客户端 Bean
 */
@Configuration
@Slf4j
public class FeishuConfig {

    private final FeishuProperties feishuProperties;

    public FeishuConfig(FeishuProperties feishuProperties) {
        this.feishuProperties = feishuProperties;
    }

    /**
     * 创建飞书 HTTP Client，用于发送消息等 API 调用
     */
    @Bean
    public Client feishuHttpClient() {
        log.info("初始化飞书客户端，AppId: {}", feishuProperties.getAppId());
        
        return new Client.Builder(
                feishuProperties.getAppId(), 
                feishuProperties.getAppSecret()
        ).build();
    }
}
