package icu.iseenu.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 股票行情数据实体
 * 用于存储??API 获取的实时行情数??
 */
@Data
public class StockMarketData {

    /**
     * 股票代码（带市场前缀，如 sh600000??
     */
    private String stockCode;

    /**
     * 股票名称
     */
    private String name;

    /**
     * 今日开盘价
     */
    private BigDecimal todayOpen;

    /**
     * 昨日收盘??
     */
    private BigDecimal yesterdayClose;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 今日最高价
     */
    private BigDecimal todayHigh;

    /**
     * 今日最低价
     */
    private BigDecimal todayLow;

    /**
     * 竞买价（买一价）
     */
    private BigDecimal bidPrice;

    /**
     * 竞卖价（卖一价）
     */
    private BigDecimal askPrice;

    /**
     * 成交的股票数（手??
     */
    private Long volume;

    /**
     * 成交金额（元??
     */
    private BigDecimal turnover;

    // 买一
    private Long bidQty1;
    private BigDecimal bidPrice1;
    
    // 买二
    private Long bidQty2;
    private BigDecimal bidPrice2;
    
    // 买三
    private Long bidQty3;
    private BigDecimal bidPrice3;
    
    // 买四
    private Long bidQty4;
    private BigDecimal bidPrice4;
    
    // 买五
    private Long bidQty5;
    private BigDecimal bidPrice5;

    // 卖一
    private Long askQty1;
    private BigDecimal askPrice1;
    
    // 卖二
    private Long askQty2;
    private BigDecimal askPrice2;
    
    // 卖三
    private Long askQty3;
    private BigDecimal askPrice3;
    
    // 卖四
    private Long askQty4;
    private BigDecimal askPrice4;
    
    // 卖五
    private Long askQty5;
    private BigDecimal askPrice5;

    /**
     * 日期（YYYY-MM-DD??
     */
    private String date;

    /**
     * 时间（HH:MM:SS??
     */
    private String time;

    /**
     * 计算涨跌??
     * @return 涨跌幅（百分比）
     */
    public BigDecimal getChangePercent() {
        if (currentPrice == null || yesterdayClose == null || yesterdayClose.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal change = currentPrice.subtract(yesterdayClose);
        return change.multiply(new BigDecimal("100")).divide(yesterdayClose, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 计算涨跌??
     * @return 涨跌??
     */
    public BigDecimal getChangeAmount() {
        if (currentPrice == null || yesterdayClose == null) {
            return null;
        }
        return currentPrice.subtract(yesterdayClose);
    }

    /**
     * 持仓数量（用于计算盈亏）
     */
    private Long holdingQuantity;

    /**
     * 持仓成本（元??
     */
    private BigDecimal holdingCost;

    /**
     * 持仓价格（元/股）
     */
    private BigDecimal holdingPrice;

    /**
     * 当前市??
     * @return 市??= 当前价格 × 持仓数量
     */
    public BigDecimal getMarketValue() {
        if (currentPrice == null || holdingQuantity == null) {
            return null;
        }
        return currentPrice.multiply(new BigDecimal(holdingQuantity));
    }

    /**
     * 浮动盈亏金额
     * @return 盈亏 = (当前价格 - 持仓价格) × 持仓数量
     */
    public BigDecimal getProfitLoss() {
        if (currentPrice == null || holdingPrice == null || holdingQuantity == null) {
            return null;
        }
        BigDecimal priceDiff = currentPrice.subtract(holdingPrice);
        return priceDiff.multiply(new BigDecimal(holdingQuantity));
    }

    /**
     * 浮动盈亏比例
     * @return 盈亏比例 = (当前价格 - 持仓价格) / 持仓价格 × 100%
     */
    public BigDecimal getProfitLossPercent() {
        if (holdingPrice == null || holdingPrice.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal priceDiff = currentPrice.subtract(holdingPrice);
        return priceDiff.multiply(new BigDecimal("100")).divide(holdingPrice, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 今日盈亏金额（基于昨日收盘价??
     * @return 今日盈亏 = (当前价格 - 昨日收盘) × 持仓数量
     */
    public BigDecimal getTodayProfitLoss() {
        if (currentPrice == null || yesterdayClose == null || holdingQuantity == null) {
            return null;
        }
        BigDecimal priceDiff = currentPrice.subtract(yesterdayClose);
        return priceDiff.multiply(new BigDecimal(holdingQuantity));
    }
}
