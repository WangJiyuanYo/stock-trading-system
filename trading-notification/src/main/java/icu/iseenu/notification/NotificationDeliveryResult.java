package icu.iseenu.notification;

/** A channel-level notification delivery outcome. */
public record NotificationDeliveryResult(String channel, boolean success, String detail) {
    public static NotificationDeliveryResult success(String channel) {
        return new NotificationDeliveryResult(channel, true, "sent");
    }

    public static NotificationDeliveryResult failure(String channel, String detail) {
        return new NotificationDeliveryResult(channel, false, detail);
    }
}
