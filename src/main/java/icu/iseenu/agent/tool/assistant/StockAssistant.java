package icu.iseenu.agent.tool.assistant;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import icu.iseenu.entity.Stock;

@AiService(
        tools = {"StockTools"}
)
public interface StockAssistant {
//    @UserMessage("""
//            根据用户输入,调用工具进行操作 {{stock}}
//            执行流程如下:
//            1. 添加或者修改股票,调用 writeJson ,如果工具返回的是true 则返回*保存成功*否则返回*保存失败*
//            2. 删除股票 调用 deleteJson 如果工具返回的是true 则返回 **删除成功** 否则返回 **删除失败**
//            """)
//    String chat(@V("stock") Stock stock);


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
            根据用户输入的自然语言描述,执行以下操作:
            
            1. 首先从用户输入中提取股票信息(stockType默认A股): {{userInput}}
            2. 如果是添加或修改股票,调用 writeJson 工具保存
               - 工具返回true则回复"**保存成功** ✅"
               - 工具返回false则回复"**保存失败** ❌"
            3. 如果是删除股票,调用 deleteJson 工具删除
               - 工具返回true则回复"**删除成功** ✅"
               - 工具返回false则回复"**删除失败** ❌"
            4. 如果用户只是查询,不调用任何工具,直接回答
            
            请明确告知用户操作结果。
            """)
    String processStockRequest(@V("userInput") String userInput);

}
