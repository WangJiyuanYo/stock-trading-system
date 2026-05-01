package icu.iseenu.agent.tool.assistant.tools;

import dev.langchain4j.agent.tool.Tool;
import icu.iseenu.entity.Stock;
import icu.iseenu.service.StockApiService;
import icu.iseenu.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class StockTools {
    @Value("${app.json.storage.path}")
    private String filePath;

    @Autowired
    private StockApiService stockApiService;

    @Autowired
    private StockService stockService;

    @Tool("以Markdown表格形式展示股票持仓及盈亏情况")
    public String getStockTableWithProfit() {
        try {
            // 获取所有股票及其行情数据（包含盈亏计算）
            List<icu.iseenu.entity.StockMarketData> marketDataList = stockApiService.fetchAllStockMarketDataWithProfit();

            if (marketDataList == null || marketDataList.isEmpty()) {
                return "暂无股票数据";
            }

            // 构建Markdown表格
            StringBuilder markdown = new StringBuilder();
            markdown.append("| 股票名称 | 持仓价格 | 持仓数量 | 当前价格 | 浮盈 |\n");
            markdown.append("|---------|---------|---------|---------|------|\n");

            for (icu.iseenu.entity.StockMarketData data : marketDataList) {
                String name = data.getName() != null ? data.getName() : "未知";

                // 持仓价格（每股成本）
                String holdingPrice = "-";
                if (data.getHoldingPrice() != null) {
                    holdingPrice = String.format("%.2f", data.getHoldingPrice());
                }

                // 持仓数量
                String holdingQuantity = "-";
                if (data.getHoldingQuantity() != null) {
                    holdingQuantity = String.valueOf(data.getHoldingQuantity());
                }

                // 当前价格
                String currentPrice = "-";
                if (data.getCurrentPrice() != null) {
                    currentPrice = String.format("%.2f", data.getCurrentPrice());
                }

                // 浮盈计算：(当前价格 - 持仓价格) * 持仓数量
                String profit = "-";
                if (data.getCurrentPrice() != null && data.getHoldingPrice() != null && data.getHoldingQuantity() != null) {
                    java.math.BigDecimal profitValue = data.getCurrentPrice()
                            .subtract(data.getHoldingPrice())
                            .multiply(new java.math.BigDecimal(data.getHoldingQuantity()));

                    // 添加正负号标识
                    String sign = profitValue.compareTo(java.math.BigDecimal.ZERO) >= 0 ? "+" : "";
                    profit = sign + String.format("%.2f", profitValue);
                }

                markdown.append(String.format("| %s | %s | %s | %s | %s |\n", name, holdingPrice, holdingQuantity, currentPrice, profit));
            }

            String result = markdown.toString();
            log.info("生成股票表格:\n{}", result);
            return result;

        } catch (IOException e) {
            log.error("获取股票数据失败", e);
            return "获取股票数据失败: " + e.getMessage();
        }
    }

    @Tool("新增 编辑股票")
    public boolean writeJson(Stock stock) {
        try {
            stockService.saveOrUpdateStock(stock);
            return true;
        } catch (IOException e) {
            log.error("写入文件失败,stock:{}", stock);
        }
        return false;
    }

    @Tool("删除股票")
    public boolean deleteJson(String stockCode) {
        try {
            stockService.deleteStock(stockCode);
            return true;
        } catch (IOException e) {
            log.error("删除文件失败,stockCode:{}", stockCode);
        }
        return false;
    }

}
