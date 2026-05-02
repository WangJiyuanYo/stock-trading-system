package icu.iseenu.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通知配置属性
 * 统一管理 application.yml 中的 notification.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    
    /**
     * 启用的通知渠道（逗号分隔）
     */
    private String enabledChannels = "";
    
    /**
     * Server 酱配置
     */
    private ServerChan serverchan = new ServerChan();
    
    /**
     * NotifyMe 配置
     */
    private NotifyMe notifyme = new NotifyMe();
    
    @Data
    public static class ServerChan {
        /**
         * Server 酱 SendKey
         */
        private String sckey = "";
    }
    
    @Data
    public static class NotifyMe {
        /**
         * NotifyMe UUID
         */
        private String uuid = "";
        
        /**
         * NotifyMe 服务地址
         */
        private String baseUrl = "https://notifyme-server.wzn556.top/?";
    }
}
