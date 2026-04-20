package icu.iseenu.agent.agent;


import dev.langchain4j.agentic.declarative.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import icu.iseenu.agent.tool.assistant.tools.WriteFileTools;
import icu.iseenu.agent.tool.assistant.HolidayAssistant;
import icu.iseenu.agent.tool.assistant.StockAssistant;

/**
 * 封装的AI调用类,通过这个方法调用内部工具
 */
@AiService
public interface SupervisorAgents {
    @SupervisorAgent(
            subAgents = {HolidayAssistant.class, StockAssistant.class, WriteFileTools.class},
            maxAgentsInvocations = 5,
            responseStrategy = SupervisorResponseStrategy.LAST,
            description = "你是一个智能助理，可以调用工具箱里的工具来帮助用户")
    String chat(@UserMessage String userMessage);
}
