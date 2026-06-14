package icu.iseenu.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 通用股票行情 DTO（精简版，不含持仓/盈亏字段）
 * 跨市场（A股 / 美股 / 港股 ...）统一返回结构
 */
@Data
public class StockQuote {

    /** 用户输入的原始代码（去前缀），如 "600000" / "SPCX" */
    private String symbol;

    /** 接口侧带前缀代码，如 "sh600000" / "gb_spcx" */
    private String formattedCode;

    /** 股票名称 */
    private String name;

    /** 市场名（A股 / 美股 / 港股） */
    private String market;

    /** 当前价 / 最新成交价（盘后即收盘价） */
    private BigDecimal price;

    /** 昨收 */
    private BigDecimal previousClose;

    /** 涨跌额 */
    private BigDecimal changeAmount;

    /** 涨跌幅 (%) */
    private BigDecimal changePercent;

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;

    private String date;
    private String time;
}
