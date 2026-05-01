package icu.iseenu.notify;

public interface NotificationSender {
    void send(String title, String message);

    default String name() { return this.getClass().getSimpleName(); }
}
