package icu.iseenu.ai.agent.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import icu.iseenu.domain.entity.Stock;
import icu.iseenu.domain.entity.StockQuote;
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
            markdown.append("| 股票代码 | 股票名称 | 持仓价格 | 持仓数量 | 当前价格 | 浮盈 |\n");
            markdown.append("|---------|---------|---------|---------|---------|------|\n");

            for (icu.iseenu.domain.entity.StockMarketData data : marketDataList) {
                // 移除市场前缀，只显示纯代码
                String stockCode = data.getStockCode();
                if (stockCode != null) {
                    stockCode = stockCode.replaceAll("^(sh|sz|hk|gb_)", "").toUpperCase();
                } else {
                    stockCode = "-";
                }
                
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

                markdown.append(String.format("| %s | %s | %s | %s | %s | %s |\n",
                        stockCode, name, holdingPrice, holdingQuantity, currentPrice, profit));
            }

            String result = markdown.toString();
            log.info("生成股票表格:\n{}", result);
            return result;

        } catch (Exception e) {
            log.error("获取股票数据失败", e);
            return "获取股票数据失败: " + e.getMessage();
        }
    }

    @Tool("新增或编辑股票")
    public boolean writeJson(Stock stock) {
        try {
            stockService.saveOrUpdateStock(stock);
            return true;
        } catch (Exception e) {
            log.error("写入文件失败, stock: {}", stock);
        }
        return false;
    }

    @Tool("删除股票")
    public boolean deleteJson(String stockCode) {
        try {
            stockService.deleteStock(stockCode);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败, stockCode: {}", stockCode);
        }
        return false;
    }

    @Tool("查询任意单只股票的最新行情（自动识别 A 股 / 美股），返回价格和涨跌幅。"
            + "A 股传 6 位代码（如 600000），美股传字母代码（如 SPCX、AAPL）。不依赖持仓信息。")
    public String getQuote(String stockCode) {
        log.info("调用 getQuote, stockCode={}", stockCode);
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return "股票代码不能为空";
        }
        try {
            StockQuote q = stockApiService.fetchQuote(stockCode.trim());
            if (q == null) {
                return "未获取到行情数据：" + stockCode;
            }
            return formatQuoteMarkdown(q);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("查询行情失败 stockCode={}", stockCode, e);
            return "查询行情失败：" + e.getMessage();
        }
    }

    private String formatQuoteMarkdown(StockQuote q) {
        StringBuilder sb = new StringBuilder();
        String name = q.getName() != null ? q.getName() : "未知";
        String market = q.getMarket() != null ? q.getMarket() : "";
        String symbol = q.getSymbol() != null ? q.getSymbol() : "";
        sb.append("📈 **").append(name).append("** (").append(symbol);
        if (!market.isEmpty()) {
            sb.append(" · ").append(market);
        }
        sb.append(")\n");

        if (q.getPrice() != null) {
            sb.append("- 最新价：").append(q.getPrice());
            if (q.getChangeAmount() != null && q.getChangePercent() != null) {
                String sign = q.getChangeAmount().compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
                sb.append("（")
                        .append(sign).append(q.getChangeAmount())
                        .append(", ")
                        .append(sign).append(q.getChangePercent()).append("%")
                        .append("）");
            }
            sb.append("\n");
        }
        if (q.getPreviousClose() != null) {
            sb.append("- 昨收：").append(q.getPreviousClose()).append("\n");
        }
        if (q.getOpen() != null || q.getHigh() != null || q.getLow() != null) {
            sb.append("- 开/高/低：")
                    .append(q.getOpen() != null ? q.getOpen() : "-").append(" / ")
                    .append(q.getHigh() != null ? q.getHigh() : "-").append(" / ")
                    .append(q.getLow() != null ? q.getLow() : "-").append("\n");
        }
        if (q.getDate() != null || q.getTime() != null) {
            sb.append("- 时间：")
                    .append(q.getDate() != null ? q.getDate() : "")
                    .append(" ")
                    .append(q.getTime() != null ? q.getTime() : "");
        }
        return sb.toString();
    }
}
