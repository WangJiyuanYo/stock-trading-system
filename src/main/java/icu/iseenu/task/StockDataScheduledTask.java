package icu.iseenu.task;

import icu.iseenu.entity.StockMarketData;
import icu.iseenu.service.NotificationService;
import icu.iseenu.service.StockApiService;
import icu.iseenu.util.TradingDayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 股票数据定时任务
 * 每个交易15:01 自动获取所有持仓股票的行情数据并计算盈??
 */
@Component
public class StockDataScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(StockDataScheduledTask.class);

    private final StockApiService stockApiService;
    private final NotificationService notificationService;
    private final TradingDayUtil tradingDayUtil;

    public StockDataScheduledTask(StockApiService stockApiService,
                                  NotificationService notificationService,
                                  TradingDayUtil tradingDayUtil) {
        this.stockApiService = stockApiService;
        this.notificationService = notificationService;
        this.tradingDayUtil = tradingDayUtil;
    }

    /**
     * 每天 11:31 执行（上午收盘前??
     * cron 表达式：????????????
     */
    @Scheduled(cron = "0 31 11 * * ?")
    public void fetchDailyStockDataMorning() {
        log.info("========== 开始执行上午股票数据定时任??(11:31) ==========");
        executeTask();
    }

    /**
     * 每天 15:01 执行（下午收盘后??
     * cron 表达式：????????????
     */
    @Scheduled(cron = "0 1 15 * * ?")
    public void fetchDailyStockDataAfternoon() {
        log.info("========== 开始执行下午股票数据定时任??(15:01) ==========");
        executeTask();
    }

    /**
     * 实际执行的任务逻辑
     */
    private void executeTask() {
        log.info("========== 开始执行股票数据定时任??==========");
        log.info("执行时间：{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 判断是否为交易日
        if (!tradingDayUtil.isTradingDay()) {
            log.warn("今日非A 股交易日，跳过执行");
            return;
        }

        try {
            // 获取所有股票的行情数据（包含盈亏计算）
            List<StockMarketData> marketDataList = stockApiService.fetchAllStockMarketDataWithProfit();

            if (marketDataList == null || marketDataList.isEmpty()) {
                log.warn("未获取到任何股票数据");
                return;
            }

            // 统计今日盈亏总和
            BigDecimal totalTodayProfitLoss = BigDecimal.ZERO;

            log.info("获取??{} 只股票的数据", marketDataList.size());

            for (StockMarketData data : marketDataList) {
                BigDecimal todayProfitLoss = data.getTodayProfitLoss();

                if (todayProfitLoss != null) {
                    totalTodayProfitLoss = totalTodayProfitLoss.add(todayProfitLoss);
                } else {
                    log.warn("股票 {} 的盈亏数据为空", data.getName());
                }
            }

            // 构建消息标题
            String title = buildTitle(totalTodayProfitLoss);
            
            // 构建个股详情字符串
            String stockDetails = buildStockDetails(marketDataList);
            
            // 构建消息内容
            String message = buildDesp(marketDataList.size(), totalTodayProfitLoss, stockDetails);
            
            // 发送通知推送
            sendNotification(title, message);

            log.info("========== 定时任务执行完成 ==========");

        } catch (IOException e) {
            log.error("执行股票数据定时任务失败：{}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("执行股票数据定时任务发生异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 手动触发股票数据获取任务
     * 用于通过 API 接口手动调用
     *
     * @return 执行结果摘要
     */
    public String executeManual() {
        log.info("========== 手动触发股票数据获取任务 ==========");
        log.info("触发时间：{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            // 获取所有股票的行情数据（包含盈亏计算）
            List<StockMarketData> marketDataList = stockApiService.fetchAllStockMarketDataWithProfit();

            if (marketDataList == null || marketDataList.isEmpty()) {
                log.warn("未获取到任何股票数据");
                return "未获取到任何股票数据";
            }

            // 统计今日盈亏总和
            BigDecimal totalTodayProfitLoss = BigDecimal.ZERO;
            int successCount = 0;

            log.info("获取??{} 只股票的数据", marketDataList.size());

            for (StockMarketData data : marketDataList) {
                String stockName = data.getName();
                BigDecimal todayProfitLoss = data.getTodayProfitLoss();

                if (todayProfitLoss != null) {
                    totalTodayProfitLoss = totalTodayProfitLoss.add(todayProfitLoss);
                    successCount++;

                    // 记录每只股票的盈亏情况
                    log.info("股票：{} | 今日盈亏：{} 元", stockName, todayProfitLoss.setScale(2, BigDecimal.ROUND_HALF_UP));
                } else {
                    log.info("股票：{} | 今日盈亏：无法计算", stockName);
                }
            }

            // 记录汇总信息
            log.info("========== 今日盈亏汇总 ==========");
            log.info("总股票数：{}", marketDataList.size());
            log.info("成功计算数：{}", successCount);
            log.info("今日总盈亏：{} 元", totalTodayProfitLoss.setScale(2, BigDecimal.ROUND_HALF_UP));
        
            if (totalTodayProfitLoss.compareTo(BigDecimal.ZERO) > 0) {
                log.info("今日表现：盈利✅");
            } else if (totalTodayProfitLoss.compareTo(BigDecimal.ZERO) < 0) {
                log.info("今日表现：亏损❌");
            } else {
                log.info("今日表现：持平➖");
            }
        
            // 构建消息标题
            String title = buildTitle(totalTodayProfitLoss);
            
            // 构建个股详情字符串
            String stockDetails = buildStockDetails(marketDataList);
            
            // 构建消息内容
            String message = buildDesp(marketDataList.size(), totalTodayProfitLoss, stockDetails);
            
            // 发送通知推送
            sendNotification(title, message);
        
            log.info("========== 手动任务执行完成 ==========");
        
            return String.format("成功获取 %d 只股票数据，今日总盈亏：%s 元",
                    marketDataList.size(),
                    totalTodayProfitLoss.setScale(2, BigDecimal.ROUND_HALF_UP));
        
        } catch (IOException e) {
            log.error("执行股票数据手动任务失败：{}", e.getMessage(), e);
            return "执行失败: " + e.getMessage();
        } catch (Exception e) {
            log.error("执行股票数据手动任务发生异常：{}", e.getMessage(), e);
            return "执行异常: " + e.getMessage();
        }
    }

    /**
     * 构建消息标题
     */
    private String buildTitle(BigDecimal totalProfitLoss) {
        String emoji;
        if (totalProfitLoss.compareTo(BigDecimal.ZERO) > 0) {
            emoji = "📈";
        } else if (totalProfitLoss.compareTo(BigDecimal.ZERO) < 0) {
            emoji = "📉";
        } else {
            emoji = "➖";
        }
        
        String profitText = totalProfitLoss.compareTo(BigDecimal.ZERO) > 0 ? "盈利" : 
                           (totalProfitLoss.compareTo(BigDecimal.ZERO) < 0 ? "亏损" : "持平");
        return String.format("%s 股票日报 - 今日%s %.2f 元", emoji, profitText, totalProfitLoss.abs().doubleValue());
    }

    /**
     * 构建个股详情字符串
     */
    private String buildStockDetails(List<StockMarketData> marketDataList) {
        StringBuilder stockDetails = new StringBuilder();
        for (StockMarketData data : marketDataList) {
            BigDecimal profit = data.getTodayProfitLoss();
            if (profit != null) {
                stockDetails.append(String.format("%s: %s %.2f 元\n\n",
                        data.getName(),
                        profit.compareTo(BigDecimal.ZERO) >= 0 ? "✅" : "❌",
                        profit));
            }
        }
        return stockDetails.toString();
    }

    /**
     * 发送通知
     */
    private void sendNotification(String title, String message) {
        try {
            notificationService.sendAlert(title, message);
            log.info("通知推送成功✅");
        } catch (Exception e) {
            log.error("通知推送失败❌: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建消息内容（Markdown 格式）
     */
    private String buildDesp(int stockCount, BigDecimal totalProfitLoss, String stockDetails) {
        StringBuilder desp = new StringBuilder();
        
        // 汇总信息
        desp.append("## 📊 今日持仓概览\n\n");
        desp.append(String.format("**持仓股票数**: %d 只\n", stockCount));
        desp.append(String.format("**今日盈亏**: %s %.2f 元\n\n", 
                totalProfitLoss.compareTo(BigDecimal.ZERO) >= 0 ? "✅" : "❌", totalProfitLoss.doubleValue()));
        
        // 明细信息
        if (stockDetails != null && !stockDetails.trim().isEmpty()) {
            desp.append("## 📝 个股详情\n\n");
            desp.append(stockDetails);
        }
        
        // 时间戳
        desp.append("\n\n---\n");
        desp.append(String.format("*推送时间*: %s", 
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        return desp.toString();
    }
}
