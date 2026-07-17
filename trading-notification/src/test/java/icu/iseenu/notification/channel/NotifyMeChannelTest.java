package icu.iseenu.notification.channel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotifyMeChannelTest {

    @Test
    void shouldConvertMarkdownImageToPlainImageUrl() {
        String message = "远行商人已刷新\n\n![商品详情](https://example.com/merchant.jpg)";

        assertThat(NotifyMeChannel.formatMessage(message))
                .isEqualTo("远行商人已刷新\n\n商品详情：https://example.com/merchant.jpg");
    }

    @Test
    void shouldConvertMultipleMarkdownImages() {
        String message = "![](https://example.com/a.png)\n![截图](https://example.com/b.jpg)";

        assertThat(NotifyMeChannel.formatMessage(message))
                .isEqualTo("图片：https://example.com/a.png\n截图：https://example.com/b.jpg");
    }

    @Test
    void shouldKeepNormalTextUnchanged() {
        assertThat(NotifyMeChannel.formatMessage("普通通知：https://example.com/detail"))
                .isEqualTo("普通通知：https://example.com/detail");
        assertThat(NotifyMeChannel.formatMessage(null)).isEmpty();
    }
}
