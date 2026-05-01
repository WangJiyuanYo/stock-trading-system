package icu.iseenu.agent.tool.assistant;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import icu.iseenu.entity.Stock;

@AiService(
        tools = {"StockTools"},
        toolProvider = "skills"
)
public interface StockAssistant {
    @SystemMessage("""
            从以下文本中提取股票信息并返回Stock对象：{{it}}
            stockType 默认赋值为 A 股
            """)
    Stock extractStockFrom(String text);

    @UserMessage("""
            调用 getStockTableWithProfit 工具以Markdown表格形式展示股票持仓及盈亏情况
            需要在最后的返回结果的头部和尾部添加 <pre> </pre> 标签
            """)
    String getStockTableWithProfit(String message);


    @UserMessage("""
            处理用户的股票操作请求: {{userInput}}
            
                        根据用户意图自动调用相应工具:
                        - 添加/修改股票 → writeJson
                        - 删除股票 → deleteJson
            
                        操作完成后用简洁的中文回复结果(成功✅/失败❌)。
            """)
    String processStockRequest(@V("userInput") String userInput);
//                根据用户输入的自然语言描述,执行以下操作:
//
//            1. 首先从用户输入中提取股票信息(stockType默认A股): {{userInput}}
//            2. 如果是添加或修改股票,调用 writeJson 工具保存
//               - 工具返回true则回复"**保存成功** ✅"
//               - 工具返回false则回复"**保存失败** ❌"
//            3. 如果是删除股票,调用 deleteJson 工具删除
//               - 工具返回true则回复"**删除成功** ✅"
//               - 工具返回false则回复"**删除失败** ❌"
//            4. 如果用户只是查询,不调用任何工具,直接回答
//
//            请明确告知用户操作结果。


}
