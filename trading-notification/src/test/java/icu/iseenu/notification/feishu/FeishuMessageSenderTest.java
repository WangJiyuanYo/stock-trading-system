package icu.iseenu.notification.feishu;

import icu.iseenu.infra.config.NotificationProperties;
import icu.iseenu.notification.channel.NotificationChannel;
import com.google.gson.JsonParser;
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

    @Test
    void shouldBuildInteractiveWebhookPayload() {
        String payload = FeishuMessageSender.buildWebhookPayload("测试标题", "测试内容");
        var json = JsonParser.parseString(payload).getAsJsonObject();

        assertEquals("interactive", json.get("msg_type").getAsString());
        assertEquals("测试标题", json.getAsJsonObject("card")
                .getAsJsonObject("header").getAsJsonObject("title")
                .get("content").getAsString());
    }

    @Test
    void shouldSplitMarkdownImageIntoTextAndImageLink() {
        String message = "远行商人已刷新\n\n![商品详情](https://example.com/merchant.jpg)";

        assertEquals("远行商人已刷新", FeishuMessageSender.removeMarkdownImages(message));
        assertEquals(1, FeishuMessageSender.extractMarkdownImages(message).size());
        assertEquals("商品详情", FeishuMessageSender.extractMarkdownImages(message).get(0).label());
        assertEquals("https://example.com/merchant.jpg",
                FeishuMessageSender.extractMarkdownImages(message).get(0).url());

        var imageLink = JsonParser.parseString(
                FeishuMessageSender.buildTextWebhookPayload("商品详情：https://example.com/merchant.jpg"))
                .getAsJsonObject();
        assertEquals("text", imageLink.get("msg_type").getAsString());
    }

    @Test
    void shouldBuildImageWebhookPayload() {
        var payload = JsonParser.parseString(
                FeishuMessageSender.buildImageWebhookPayload("img_test_key"))
                .getAsJsonObject();

        assertEquals("image", payload.get("msg_type").getAsString());
        assertEquals("img_test_key", payload.getAsJsonObject("content")
                .get("image_key").getAsString());
    }
}
