package icu.iseenu.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 飞书配置属性
 * 统一管理 application.yml 中的 feishu.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "feishu")
public class FeishuProperties {
    
    /**
     * 飞书应用 App ID
     */
    private String appId = "";
    
    /**
     * 飞书应用 App Secret
     */
    private String appSecret = "";
}
