package icu.iseenu.feishu.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import icu.iseenu.ai.agent.supervisor.SupervisorAgents;
import icu.iseenu.notification.feishu.FeishuMessageSender;
import icu.iseenu.roco.model.TemplateData;
import icu.iseenu.roco.service.MerchantScreenshotAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class FeishuService {

    private final FeishuMessageSender messageSender;

    private SupervisorAgents supervisorAgents;
    private MerchantScreenshotAsyncService screenshotAsyncService;

    public FeishuService(FeishuMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Autowired(required = false)
    public void setSupervisorAgents(SupervisorAgents supervisorAgents) {
        this.supervisorAgents = supervisorAgents;
    }

    @Autowired(required = false)
    public void setMerchantScreenshotAsyncService(MerchantScreenshotAsyncService screenshotAsyncService) {
        this.screenshotAsyncService = screenshotAsyncService;
    }

    /**
     * 处理飞书接收到的消息，调用 AI Agent 并回复
     */
    public void resolveEvent(String chatId, String senderId, String messageContent) {
        if (supervisorAgents == null) {
            log.warn("SupervisorAgents 未初始化");
            messageSender.sendMarkdownMessage(senderId, "AI 服务未配置，请设置 DEEPSEEK_API_KEY 后重启");
            return;
        }

        String userText;
        try {
            JsonObject json = JsonParser.parseString(messageContent).getAsJsonObject();
            userText = json.has("text") ? json.get("text").getAsString() : messageContent;
        } catch (Exception e) {
            log.warn("解析消息内容失败，使用原始内容: {}", messageContent);
            userText = messageContent;
        }

        if (userText == null || userText.trim().isEmpty()) {
            return;
        }

        log.info("飞书消息: chatId={}, text={}", chatId, userText);

        boolean isMerchantQuery = isRocoMerchantQuery(userText);
        if (isMerchantQuery) {
            messageSender.sendMarkdownMessage(senderId, "⏳ 正在查询远行商人数据，请稍候...");
        }

        try {
            String reply = supervisorAgents.chat(chatId, userText);
            log.info("Agent 回复: {}", reply);

            if (isMerchantQuery) {
                triggerAsyncScreenshot(senderId);
            } else {
                messageSender.sendMarkdownMessage(senderId, reply);
            }
        } catch (Exception e) {
            log.error("Agent 调用失败", e);
            messageSender.sendMarkdownMessage(senderId, "处理失败: " + e.getMessage());
        }
    }

    private boolean isRocoMerchantQuery(String userText) {
        return userText != null && (userText.contains("远行商人")
                || userText.contains("商人")
                || userText.contains("商品"));
    }

    private void triggerAsyncScreenshot(String senderId) {
        CompletableFuture.runAsync(() -> {
            try {
                var rawData = screenshotAsyncService.fetchGameData();
                if (rawData == null) {
                    messageSender.sendMarkdownMessage(senderId, "查询远行商人失败：无法获取游戏数据");
                    return;
                }

                TemplateData templateData = screenshotAsyncService.processData(rawData);
                if (templateData.getProductCount() == 0) {
                    messageSender.sendMarkdownMessage(senderId, "远行商人当前暂无商品");
                    return;
                }

                String imageUrl = screenshotAsyncService.generateAndUploadScreenshot(templateData);
                if (imageUrl != null) {
                    messageSender.sendImageMessage(senderId, imageUrl);
                } else {
                    messageSender.sendMarkdownMessage(senderId, "商品截图生成失败，请稍后重试");
                }
            } catch (Exception e) {
                log.error("异步截图生成失败", e);
                messageSender.sendMarkdownMessage(senderId, "查询远行商人失败: " + e.getMessage());
            }
        });
    }
}
