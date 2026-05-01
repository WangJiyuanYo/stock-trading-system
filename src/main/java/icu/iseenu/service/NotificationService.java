package icu.iseenu.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final CompositeNotificationSender compositeNotificationSender;

    // 构造器注入，使用组合通知发送器支持多渠道通知
    public NotificationService(CompositeNotificationSender compositeNotificationSender) {
        this.compositeNotificationSender = compositeNotificationSender;
    }

    public void sendAlert(String title, String content) {
        compositeNotificationSender.send(title, content);
    }
}
