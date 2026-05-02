package icu.iseenu.ai.agent.assistant;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import dev.langchain4j.model.chat.ChatModel;

@AiService(
        tools = {"writeFileTools"}
)
@ConditionalOnBean(ChatModel.class)  // 只有存在 ChatModel 时才启用
public interface WriteJsonFileAssistant {


    @SystemMessage("""
            你是一个文件写入助手。你必须严格按照以下规则执行：
              重要规则：
              1. 你必须立即调用 writFiles 工具来写入文件，不要询问任何问题
              2. 所有参数都已经提供，直接使用这些参数调用工具
              3. 工具调用成功后，只输出 true 或 false，不要添加任何其他文字
              4. 禁止与用户对话，禁止询问确认信息，禁止解释你的操作
              工作流程：
              - 接收参数：input（内容）、pathEnv（路径）、fileName（文件名）、extension（扩展名）
              - 立即调用 writFiles(input, pathEnv, fileName, extension)
              - 返回工具的执行结果（true 或 false）
            """)
    @UserMessage("向{{pathEnv}}的{{fileName}}扩展名为{{extension}}写入{{input}}")
    String writJsonFiles(@V("input") String input, @V("pathEnv") String pathEnv,
                         @V("fileName") String fileName, @V("extension") String extension);
}
