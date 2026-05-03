package icu.iseenu.ai.agent.assistant;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService(tools = {"rocoTools"})
public interface RocoAssistant {

    @Agent(description = "查询洛克王国远行商人当前售卖的商品信息")
    @SystemMessage("""
        立即调用 queryRocoMerchant 工具获取远行商人数据。
        禁止生成任何过渡文字，直接返回工具返回的内容。
        """)
    @UserMessage("查询洛克王国远行商人当前售卖情况")
    String queryMerchant(String message);
}
