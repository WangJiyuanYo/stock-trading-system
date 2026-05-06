package icu.iseenu.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 股票实体类 - MyBatis-Plus版本
 * 用于SQLite数据库存储
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("stocks")
public class Stock {

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 股票类型（如：A股、港股、美股）
     */
    @TableField("stock_type")
    private String stockType;

    /**
     * 股票代码（唯一）
     */
    @TableField("stock_code")
    private String stockCode;

    /**
     * 持仓数量（股数）
     */
    @TableField("holding_quantity")
    private Long holdingQuantity;

    /**
     * 持仓价格（元/股）
     */
    @TableField("holding_price")
    private BigDecimal holdingPrice;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除字段
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 持仓成本（计算字段，不存储在数据库中）
     */
    @TableField(exist = false)
    private BigDecimal holdingCost;

    /**
     * 计算持仓成本
     * @return 持仓成本 = 持仓价格 × 持仓数量
     */
    public BigDecimal getHoldingCost() {
        if (holdingPrice == null || holdingQuantity == null) {
            return null;
        }
        return holdingPrice.multiply(new BigDecimal(holdingQuantity));
    }
}
