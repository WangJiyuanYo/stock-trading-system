package icu.iseenu.agent.agent;


import dev.langchain4j.agentic.declarative.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import icu.iseenu.agent.tool.assistant.HolidayAssistant;
import icu.iseenu.agent.tool.assistant.StockAssistant;
import icu.iseenu.agent.tool.assistant.WriteJsonFileAssistant;

/**
 * 封装的AI调用类,通过这个方法调用内部工具
 */
@AiService
public interface SupervisorAgents {
    @SupervisorAgent(
            subAgents = {HolidayAssistant.class, StockAssistant.class, WriteJsonFileAssistant.class},
            maxAgentsInvocations = 5,
            responseStrategy = SupervisorResponseStrategy.LAST,
            description = """
                    你是一个智能助理，可以调用子助手来完成用户的请求。
                    如果用户需要处理股票相关操作（添加、修改、删除、查询），请使用 StockAssistant。
                    如果用户需要查询节假日信息，请使用 HolidayAssistant。
                    如果用户需要写入文件，请使用WriteJsonFileAssistant。
                    """)
    String chat(@UserMessage String userMessage);
}
