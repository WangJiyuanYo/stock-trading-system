package icu.iseenu.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 股票实体??
 * 用于存储股票持仓信息
 */
@Data
public class Stock {

    /**
     * 股票类型（如：A 股、港股、美股）
     */
    private String stockType;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 持仓数量（股数）
     */
    private Long holdingQuantity;

    /**
     * 持仓价格（元/股）
     */
    private BigDecimal holdingPrice;

    public Stock() {
    }

    public Stock(String stockType, String stockCode, Long holdingQuantity, BigDecimal holdingPrice) {
        this.stockType = stockType;
        this.stockCode = stockCode;
        this.holdingQuantity = holdingQuantity;
        this.holdingPrice = holdingPrice;
    }

    /**
     * 计算持仓成本
     * @return 持仓成本 = 持仓价格 × 持仓数量
     */
    @JsonIgnore  // 忽略此字段，不序列化和反序列??
    public BigDecimal getHoldingCost() {
        if (holdingPrice == null || holdingQuantity == null) {
            return null;
        }
        return holdingPrice.multiply(new BigDecimal(holdingQuantity));
    }
}
