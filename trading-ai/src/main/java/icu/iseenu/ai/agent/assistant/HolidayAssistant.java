package icu.iseenu.ai.agent.assistant;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService(tools = "holidayTools")
public interface HolidayAssistant {

    @Agent(description = "查询指定年份的中国法定节假日，返回纯JSON格式数据")
    @SystemMessage("""
        立即调用 getHolidayOfYear 获取指定年份的节假日数据。
        然后调用 checkDataFormat 校验格式。
        如果校验失败，重新获取。
        最终只输出纯JSON，禁止输出任何解释、过渡文字或markdown标记。
        """)
    @UserMessage("获取{{year}}年的节假日信息")
    String fetchHoliday(@V("year") String year);
}
