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
    @UserMessage("""
            根据用户输入,调用工具进行操作 {{stock}}
            执行流程如下:
            1. 添加或者修改股票,调用 writeJson ,如果工具返回的是true 则返回*保存成功*否则返回*保存失败*
            2. 删除股票 调用 deleteJson 如果工具返回的是true 则返回 **删除成功** 否则返回 **删除失败**
            """)
    String chat(@V("stock") Stock stock);


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
}
