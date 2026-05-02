package icu.iseenu.roco.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 洛克王国远行商人配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "roco")
public class AppConfig {
    
    // API配置
    public static final String GAME_API_URL = "https://wegame.shallow.ink/api/v1/games/rocom/merchant/info";
    public static final String NOTIFYME_SERVER = "https://notifyme-server.wzn556.top/api/send";
    public static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";
    
    // 资源路径配置
    public static final String ASSETS_DIR = "assets/yuanxing-shangren";
    public static final String HTML_TEMPLATE_FILE = "index.html";
    public static final String TEMP_RENDER_FILE = "temp_render.html";
    public static final String SCREENSHOT_FILE = "merchant_render.jpg";
    
    // 从配置文件读取的密钥
    private String rocomApiKey;
    private String imgbbKey;
    private String notifymeUuid;
    
    // 验证配置是否完整
    public boolean hasRocomApiKey() {
        return rocomApiKey != null && !rocomApiKey.isEmpty();
    }
    
    public boolean hasImgbbKey() {
        return imgbbKey != null && !imgbbKey.isEmpty();
    }
    
    public boolean hasNotifymeUuid() {
        return notifymeUuid != null && !notifymeUuid.isEmpty();
    }
    
    public boolean hasAnyNotificationChannel() {
        return hasNotifymeUuid();
    }
}
