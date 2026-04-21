package icu.iseenu.service;

import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import icu.iseenu.agent.agent.SupervisorAgents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class FeishuService {


    private final Client client;

    private final SupervisorAgents supervisorAgents;

    public FeishuService(Client client, SupervisorAgents supervisorAgents) {
        this.client = client;
        this.supervisorAgents = supervisorAgents;
    }

    /**
     * 发送Markdown格式消息
     *
     * @param markdownContent Markdown内容
     */
    public void sendMarkdownMessage(String senderId, String markdownContent) {
        // 使用interactive卡片消息类型,支持更好的富文本展示
        // 将markdown转换为纯文本,保留基本格式
        String textContent = markdownContent.replace("# ", "").replace("## ", "").replace("|", "");

        // 构建interactive卡片消息
        StringBuilder cardBuilder = new StringBuilder();
        cardBuilder.append("{");
        cardBuilder.append("\"config\":{\"wide_screen_mode\":true},");
        cardBuilder.append("\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"消息通知\"},\"template\":\"blue\"},");
        cardBuilder.append("\"elements\":[");
        cardBuilder.append("{\"tag\":\"div\",\"text\":{\"tag\":\"lark_md\",\"content\":\"");

        // 转义特殊字符
        String escapedContent = textContent
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        cardBuilder.append(escapedContent);
        cardBuilder.append("\"}}");
        cardBuilder.append("]}");

        String content = cardBuilder.toString();
        log.debug("发送卡片消息内容: {}", content);

        // 创建请求对象
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
            // 发起请求
            CreateMessageResp resp = client.im().v1().message().create(req);

            if (!resp.success()) {
                log.error("发送卡片消息失败 - code:{},msg:{},reqId:{}, resp:{}",
                        resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
                return;
            }

            log.info("卡片消息发送成功");
        } catch (Exception e) {
            log.error("发送卡片消息异常", e);
        }
    }

    /**
     * 处理飞书接收到的消息
     *
     * @param chatId  会话ID
     * @param message 消息内容
     */
    public void resolveEvent(String chatId, String senderId, String message) {
        sendMarkdownMessage(senderId, supervisorAgents.chat(chatId, message));
    }
}
