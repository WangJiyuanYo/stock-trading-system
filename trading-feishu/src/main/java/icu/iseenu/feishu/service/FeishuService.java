package icu.iseenu.feishu.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import icu.iseenu.ai.agent.supervisor.SupervisorAgents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class FeishuService {

    private final Client client;

    private SupervisorAgents supervisorAgents;

    public FeishuService(Client client) {
        this.client = client;
    }

    @Autowired(required = false)
    public void setSupervisorAgents(SupervisorAgents supervisorAgents) {
        this.supervisorAgents = supervisorAgents;
    }

    /**
     * 发送Markdown格式消息
     */
    public void sendMarkdownMessage(String senderId, String markdownContent) {

        String textContent = markdownContent.replace("# ", "").replace("## ", "").replace("|", "");

        StringBuilder cardBuilder = new StringBuilder();
        cardBuilder.append("{");
        cardBuilder.append("\"config\":{\"wide_screen_mode\":true},");
        cardBuilder.append("\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"AI 助手\"},\"template\":\"blue\"},");
        cardBuilder.append("\"elements\":[");
        cardBuilder.append("{\"tag\":\"div\",\"text\":{\"tag\":\"lark_md\",\"content\":\"");

        String escapedContent = textContent
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        cardBuilder.append(escapedContent);
        cardBuilder.append("\"}}");
        cardBuilder.append("]}");

        String content = cardBuilder.toString();
        log.debug("发送卡片消息内容: {}", content);

        CreateMessageReq req = CreateMessageReq.newBuilder()
                .createMessageReqBody(CreateMessageReqBody.newBuilder()
                        .receiveId(senderId)
                        .msgType("interactive")
                        .content(content)
                        .uuid(UUID.randomUUID().toString())
                        .build())
                .receiveIdType("open_id")
                .build();

        try {
            CreateMessageResp resp = client.im().v1().message().create(req);

            if (!resp.success()) {
                log.error("发送卡片消息失败 - code:{},msg:{},reqId:{}, resp:{}",
                        resp.getCode(), resp.getMsg(), resp.getRequestId(),
                        Jsons.createGSON(true, false).toJson(JsonParser.parseString(
                                new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
                return;
            }

            log.info("卡片消息发送成功");
        } catch (Exception e) {
            log.error("发送卡片消息异常", e);
        }
    }

    /**
     * 处理飞书接收到的消息，调用 AI Agent 并回复
     *
     * @param chatId  会话ID（用作 memoryId）
     * @param senderId 发送者 openId
     * @param messageContent 消息原始 JSON 字符串
     */
    public void resolveEvent(String chatId, String senderId, String messageContent) {
        if (supervisorAgents == null) {
            log.warn("SupervisorAgents 未初始化");
            sendMarkdownMessage(senderId, "AI 服务未配置，请设置 DEEPSEEK_API_KEY 后重启");
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

        try {
            String reply = supervisorAgents.chat(chatId, userText);
            log.info("Agent 回复: {}", reply);
            sendMarkdownMessage(senderId, reply);
        } catch (Exception e) {
            log.error("Agent 调用失败", e);
            sendMarkdownMessage(senderId, "处理失败: " + e.getMessage());
        }
    }
}
