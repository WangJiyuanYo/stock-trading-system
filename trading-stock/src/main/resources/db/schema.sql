-- ============================================
-- 股票表创建脚本 (SQLite)
-- 对应实体: icu.iseenu.stock.entity.Stock
-- ============================================

-- 创建股票表
CREATE TABLE IF NOT EXISTS stocks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,              -- 主键ID，自增
    stock_type VARCHAR(20) NOT NULL,                   -- 股票类型（A股、港股、美股等）
    stock_code VARCHAR(20) NOT NULL UNIQUE,            -- 股票代码（唯一）
    holding_quantity INTEGER,                          -- 持仓数量（股数）
    holding_price DECIMAL(10, 2),                      -- 持仓价格（元/股）
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,    -- 创建时间
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,    -- 更新时间
    deleted INTEGER DEFAULT 0                          -- 逻辑删除字段（0-未删除，1-已删除）
);

-- 创建索引以优化查询性能
CREATE INDEX IF NOT EXISTS idx_stock_code ON stocks(stock_code);      -- 股票代码索引
CREATE INDEX IF NOT EXISTS idx_stock_type ON stocks(stock_type);      -- 股票类型索引
CREATE INDEX IF NOT EXISTS idx_deleted ON stocks(deleted);            -- 逻辑删除索引
CREATE INDEX IF NOT EXISTS idx_create_time ON stocks(create_time);    -- 创建时间索引
