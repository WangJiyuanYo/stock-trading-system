package icu.iseenu.stock.converter;

import icu.iseenu.domain.entity.Stock;

/**
 * 股票实体转换器
 * 用于在 domain.entity.Stock 和 stock.entity.Stock 之间转换
 */
public class StockConverter {

    /**
     * 将数据库实体转换为领域实体
     * @param stockEntity 数据库实体
     * @return 领域实体
     */
    public static Stock toDomain(icu.iseenu.stock.entity.Stock stockEntity) {
        if (stockEntity == null) {
            return null;
        }
        Stock stock = new Stock();
        stock.setStockType(stockEntity.getStockType());
        stock.setStockCode(stockEntity.getStockCode());
        stock.setHoldingQuantity(stockEntity.getHoldingQuantity());
        stock.setHoldingPrice(stockEntity.getHoldingPrice());
        return stock;
    }

    /**
     * 将领域实体转换为数据库实体
     * @param stock 领域实体
     * @return 数据库实体
     */
    public static icu.iseenu.stock.entity.Stock toEntity(Stock stock) {
        if (stock == null) {
            return null;
        }
        icu.iseenu.stock.entity.Stock stockEntity = new icu.iseenu.stock.entity.Stock();
        stockEntity.setStockType(stock.getStockType());
        stockEntity.setStockCode(stock.getStockCode());
        stockEntity.setHoldingQuantity(stock.getHoldingQuantity());
        stockEntity.setHoldingPrice(stock.getHoldingPrice());
        return stockEntity;
    }

    /**
     * 将数据库实体列表转换为领域实体列表
     * @param stockEntities 数据库实体列表
     * @return 领域实体列表
     */
    public static java.util.List<Stock> toDomainList(java.util.List<icu.iseenu.stock.entity.Stock> stockEntities) {
        if (stockEntities == null) {
            return new java.util.ArrayList<>();
        }
        java.util.List<Stock> stocks = new java.util.ArrayList<>();
        for (icu.iseenu.stock.entity.Stock entity : stockEntities) {
            stocks.add(toDomain(entity));
        }
        return stocks;
    }

    /**
     * 将领域实体列表转换为数据库实体列表
     * @param stocks 领域实体列表
     * @return 数据库实体列表
     */
    public static java.util.List<icu.iseenu.stock.entity.Stock> toEntityList(java.util.List<Stock> stocks) {
        if (stocks == null) {
            return new java.util.ArrayList<>();
        }
        java.util.List<icu.iseenu.stock.entity.Stock> entities = new java.util.ArrayList<>();
        for (Stock stock : stocks) {
            entities.add(toEntity(stock));
        }
        return entities;
    }
}
