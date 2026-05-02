package icu.iseenu.feishu.config;

import com.lark.oapi.Client;
import icu.iseenu.infra.config.FeishuProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 飞书配置
 * 创建飞书客户端 Bean
 */
@Configuration
@ConditionalOnProperty(name = "feishu.app-id")  // 只有配置了 feishu.app-id 时才启用
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
