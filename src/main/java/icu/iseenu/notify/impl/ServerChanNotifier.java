package icu.iseenu.notify.impl;

import icu.iseenu.notify.NotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service("serverChanNotifier")
public class ServerChanNotifier implements NotificationSender {
    @Value("${serverchan.sendkey:}")
    private String sendKey;

    @Override
    public void send(String title, String message) {
        // TODO: 实现ServerChan通知发送逻辑
        System.out.println("Sending ServerChan notification: " + title + " - " + message);
    }

    @Override
    public String name() {
        return "serverchan";
    }
}
