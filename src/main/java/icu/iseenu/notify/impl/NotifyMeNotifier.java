package icu.iseenu.notify.impl;

import icu.iseenu.notify.NotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("notifyMeNotifier")
public class NotifyMeNotifier implements NotificationSender {
//    @Value("${notification.notifyme.api-key}")
//    private String apiKey;
    @Value("${notification.notifyme.base-url:https://api.notifyme.com}")
    private String baseUrl;

    @Override
    public void send(String title, String message) {
        // TODO: 实现NotifyMe通知发送逻辑
        System.out.println("Sending NotifyMe notification: " + title + " - " + message);
    }

    @Override
    public String name() {
        return "notifyme";
    }
}
