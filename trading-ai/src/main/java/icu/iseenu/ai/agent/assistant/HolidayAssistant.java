package icu.iseenu.ai.agent.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import dev.langchain4j.model.chat.ChatModel;


@AiService(
        tools = "holidayTools"
)
@ConditionalOnBean(ChatModel.class)  // 只有存在 ChatModel 时才启用
public interface HolidayAssistant {

    @SystemMessage("""
            你是一个中国节假日信息获取助手，工作流程如下：
            1.调用 getHolidayOfYear 获取数据
            2.调用 checkDataFormat 校验获取的数据
            3.如果校验失败，重新获取再次校验
            4.最终回答必须只输出纯JSON格式，不要包含任何markdown标记、解释文字或其他内容。
            5.调用 checkDataFormat 校验数据格式,校验通过后返回结果
            """)
    @UserMessage("获取{{year}}这一年的节假日信息")
    String fetchHoliday(@V("year") String year);
}
