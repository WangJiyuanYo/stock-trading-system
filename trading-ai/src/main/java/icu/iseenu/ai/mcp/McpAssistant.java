package icu.iseenu.ai.mcp;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@AiService(toolProvider = "mcpToolProvider", chatModel = "chatModel")
@ConditionalOnBean(name = "mcpToolProvider")
public interface McpAssistant {
    @SystemMessage("""
            调用MCP服务，返回MCP所返回的结果，不需要添加其他的解释和语句
            """)
    String chat(@UserMessage String question);
}
