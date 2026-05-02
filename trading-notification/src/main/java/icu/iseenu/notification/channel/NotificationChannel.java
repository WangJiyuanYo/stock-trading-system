package icu.iseenu.notification.channel;

/**
 * 通知渠道接口
 * 每个实现类代表一个通知渠道（如 Server 酱、NotifyMe 等）
 */
public interface NotificationChannel {
    
    /**
     * 发送通知
     * @param title 标题
     * @param message 消息内容
     */
    void send(String title, String message);
    
    /**
     * 渠道名称（用于配置和日志）
     * @return 渠道名称
     */
    String getName();
    
    /**
     * 是否启用
     * @return true 表示启用
     */
    default boolean isEnabled() {
        return true;
    }
}
