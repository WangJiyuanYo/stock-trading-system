package icu.iseenu.notification.channel;

import icu.iseenu.notification.NotificationDeliveryResult;

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
     * 按业务场景发送通知。
     * 不需要场景路由的渠道默认沿用普通发送逻辑。
     *
     * @param scene 通知场景，如 stock、roco
     * @param title 标题
     * @param message 消息内容
     */
    default void send(String scene, String title, String message) {
        send(title, message);
    }

    default NotificationDeliveryResult sendWithResult(String scene, String title, String message) {
        try {
            send(scene, title, message);
            return NotificationDeliveryResult.success(getName());
        } catch (Exception e) {
            return NotificationDeliveryResult.failure(getName(), e.getMessage());
        }
    }
    
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
