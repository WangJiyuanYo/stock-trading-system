package icu.iseenu.ai.agent.assistant;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService(tools = {"writeFileTools"})
public interface WriteJsonFileAssistant {

    @Agent(description = "向指定路径的JSON文件写入内容")
    @SystemMessage("""
        立即调用 writFiles 工具写入文件。禁止生成任何解释或过渡文字。
        工具调用后只返回 true 或 false。
        """)
    @UserMessage("向{{pathEnv}}的{{fileName}}扩展名为{{extension}}写入{{input}}")
    String writJsonFiles(@V("input") String input, @V("pathEnv") String pathEnv,
                         @V("fileName") String fileName, @V("extension") String extension);
}
