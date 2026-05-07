package icu.iseenu.ai.agent.assistant;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService(tools = {"rocoTools"})
public interface RocoAssistant {

    @Agent(description = "仅用于查询远行商人售卖情况，不处理家园/种植/农场相关请求")
    @SystemMessage("""
        调用 queryRocoMerchant 工具获取远行商人数据。
        如果用户询问的是家园、种植、农场等信息，立即停止并告知用户重新描述需求。
        禁止生成任何过渡文字，直接返回工具返回的内容。
        """)
    @UserMessage("查询洛克王国远行商人当前售卖情况")
    String queryMerchant(String message);

    @Agent(description = "查询洛克王国玩家的家园种植信息，根据UID获取种植的作物及预计成熟时间")
    @SystemMessage("""
        调用 queryFarmInfo 工具获取家园种植数据，从用户消息中提取UID数字作为参数。
        如果用户未提供UID，回复提示用户提供UID。
        禁止生成任何过渡文字，直接返回工具返回的内容。
        """)
    @UserMessage("查询洛克王国家园种植信息: {{message}}")
    String queryFarm(String message);
}
