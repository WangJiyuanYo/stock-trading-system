package icu.iseenu.ai.agent.supervisor;

import dev.langchain4j.agentic.declarative.SupervisorAgent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import icu.iseenu.ai.agent.assistant.HolidayAssistant;
import icu.iseenu.ai.agent.assistant.RocoAssistant;
import icu.iseenu.ai.agent.assistant.StockAssistant;

@AiService(
        chatMemoryProvider = "chatMemoryProvider"
)

public interface SupervisorAgents {

    @SupervisorAgent(
            subAgents = {HolidayAssistant.class, StockAssistant.class, RocoAssistant.class},
            maxAgentsInvocations = 5,
            description = "你是一个智能助理，可以调用工具箱里的工具来帮助用户")

    String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
