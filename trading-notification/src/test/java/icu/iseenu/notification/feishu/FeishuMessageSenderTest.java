package icu.iseenu.notification.feishu;

import icu.iseenu.infra.config.NotificationProperties;
import icu.iseenu.notification.channel.NotificationChannel;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class FeishuMessageSenderTest {

    private final NotificationProperties props = new NotificationProperties();

    @Test
    void shouldImplementNotificationChannel() {
        FeishuMessageSender sender = new FeishuMessageSender(props, null,
                WebClient.builder());

        assertInstanceOf(NotificationChannel.class, sender,
                "FeishuMessageSender must implement NotificationChannel");
    }

    @Test
    void getNameShouldReturnFeishu() {
        FeishuMessageSender sender = new FeishuMessageSender(props, null,
                WebClient.builder());

        assertEquals("feishu", sender.getName());
    }

    @Test
    void isEnabledWhenChannelInList() {
        props.setEnabledChannels("serverchan,notifyme,feishu");
        FeishuMessageSender sender = new FeishuMessageSender(props, null,
                WebClient.builder());

        assertTrue(sender.isEnabled());
    }

    @Test
    void isDisabledWhenChannelNotInList() {
        props.setEnabledChannels("serverchan,notifyme");
        FeishuMessageSender sender = new FeishuMessageSender(props, null,
                WebClient.builder());

        assertFalse(sender.isEnabled());
    }

    @Test
    void isDisabledWhenEnabledChannelsNull() {
        props.setEnabledChannels(null);
        FeishuMessageSender sender = new FeishuMessageSender(props, null,
                WebClient.builder());

        assertFalse(sender.isEnabled());
    }

    @Test
    void sendSkipsWhenWebhookUrlEmpty() {
        props.getFeishu().setWebhookUrl("");
        FeishuMessageSender sender = new FeishuMessageSender(props, null,
                WebClient.builder());

        // Should not throw, just log a warning and return
        assertDoesNotThrow(() -> sender.send("测试标题", "测试内容"));
    }

    @Test
    void sendMarkdownSkipsWhenClientNull() {
        FeishuMessageSender sender = new FeishuMessageSender(props, null,
                WebClient.builder());

        assertDoesNotThrow(() -> sender.sendMarkdownMessage("user123", "Hello"));
    }

    @Test
    void sendImageSkipsWhenClientNull() {
        FeishuMessageSender sender = new FeishuMessageSender(props, null,
                WebClient.builder());

        assertDoesNotThrow(() -> sender.sendImageMessage("user123", "http://example.com/img.png"));
    }
}
