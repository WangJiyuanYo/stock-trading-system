package icu.iseenu.ai.agent.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import icu.iseenu.domain.entity.Stock;
import icu.iseenu.stock.api.StockApiService;
import icu.iseenu.stock.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class StockTools {

    private final String filePath;
    private final StockApiService stockApiService;
    private final StockService stockService;

    public StockTools(@Value("${app.json.storage.path}") String filePath,
                      StockApiService stockApiService,
                      StockService stockService) {
        this.filePath = filePath;
        this.stockApiService = stockApiService;
        this.stockService = stockService;
    }

    @Tool("以Markdown表格形式展示股票持仓及盈亏情况")
    public String getStockTableWithProfit() {
        log.info("调用getStockTableWithProfit接口");
        try {
            List<icu.iseenu.domain.entity.StockMarketData> marketDataList =
                    stockApiService.fetchAllStockMarketDataWithProfit();

            if (marketDataList == null || marketDataList.isEmpty()) {
                return "暂无股票数据";
            }

            StringBuilder markdown = new StringBuilder();
            markdown.append("| 股票名称 | 持仓价格 | 持仓数量 | 当前价格 | 浮盈 |\n");
            markdown.append("|---------|---------|---------|---------|------|\n");

            for (icu.iseenu.domain.entity.StockMarketData data : marketDataList) {
                String name = data.getName() != null ? data.getName() : "未知";

                String holdingPrice = "-";
                if (data.getHoldingPrice() != null) {
                    holdingPrice = String.format("%.2f", data.getHoldingPrice());
                }

                String holdingQuantity = "-";
                if (data.getHoldingQuantity() != null) {
                    holdingQuantity = String.valueOf(data.getHoldingQuantity());
                }

                String currentPrice = "-";
                if (data.getCurrentPrice() != null) {
                    currentPrice = String.format("%.2f", data.getCurrentPrice());
                }

                String profit = "-";
                if (data.getCurrentPrice() != null && data.getHoldingPrice() != null
                        && data.getHoldingQuantity() != null) {
                    BigDecimal profitValue = data.getCurrentPrice()
                            .subtract(data.getHoldingPrice())
                            .multiply(new BigDecimal(data.getHoldingQuantity()));

                    String sign = profitValue.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
                    profit = sign + String.format("%.2f", profitValue);
                }

                markdown.append(String.format("| %s | %s | %s | %s | %s |\n",
                        name, holdingPrice, holdingQuantity, currentPrice, profit));
            }

            String result = markdown.toString();
            log.info("生成股票表格:\n{}", result);
            return result;

        } catch (IOException e) {
            log.error("获取股票数据失败", e);
            return "获取股票数据失败: " + e.getMessage();
        }
    }

    @Tool("新增或编辑股票")
    public boolean writeJson(Stock stock) {
        try {
            stockService.saveOrUpdateStock(stock);
            return true;
        } catch (IOException e) {
            log.error("写入文件失败, stock: {}", stock);
        }
        return false;
    }

    @Tool("删除股票")
    public boolean deleteJson(String stockCode) {
        try {
            stockService.deleteStock(stockCode);
            return true;
        } catch (IOException e) {
            log.error("删除文件失败, stockCode: {}", stockCode);
        }
        return false;
    }
}
