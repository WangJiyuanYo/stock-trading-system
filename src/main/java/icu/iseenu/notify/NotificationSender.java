package icu.iseenu.notify;

public interface NotificationSender {
    void send(String title, String message);

    // 可选：增加一个方法返回渠道名称，便于调试
    default String name() { return this.getClass().getSimpleName(); }
}
