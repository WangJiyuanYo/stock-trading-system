package icu.iseenu.ai.agent.assistant;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import icu.iseenu.domain.entity.Stock;

@AiService(
    tools = {"stockTools"},
    toolProvider = "skills"
)
public interface StockAssistant {

    @Agent(description = "从文本中提取股票信息，返回Stock对象")
    @SystemMessage("从以下文本中提取股票信息并返回Stock对象：{{it}}。stockType 默认赋值为 A 股")
    Stock extractStockFrom(@UserMessage String text);

    @Agent(description = "查询股票持仓和盈亏情况，返回Markdown表格")
    @SystemMessage("立即调用 getStockTableWithProfit 工具获取股票持仓数据。禁止生成任何过渡文字，直接返回工具返回的内容。")
    @UserMessage("展示股票持仓及盈亏情况")
    String getStockTableWithProfit(String message);

    @Agent(description = "添加、修改或删除股票。根据用户输入的自然语言自动调用 writeJson 或 deleteJson 工具")
    @SystemMessage("""
        立即调用工具处理以下请求，禁止生成过渡文字。
        - 添加/修改股票 → 立即调用 writeJson
        - 删除股票 → 立即调用 deleteJson
        操作完成后简洁回复：成功✅ 或 失败❌ + 原因。
        """)
    @UserMessage("{{userInput}}")
    String processStockRequest(@V("userInput") String userInput);
}
