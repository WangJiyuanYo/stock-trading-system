package icu.iseenu.stock.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.iseenu.common.exception.ResourceNotFoundException;
import icu.iseenu.common.exception.ValidationException;
import icu.iseenu.domain.entity.Stock;
import icu.iseenu.domain.enums.StockTypeEnum;
import icu.iseenu.stock.converter.StockConverter;
import icu.iseenu.stock.mapper.StockMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 股票业务服务
 * 提供股票数据的校验、保存等业务逻辑
 */
@Service
public class StockService {

    private final StockMapper stockMapper;

    public StockService(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    /**
     * 验证股票信息
     *
     * @param stock 股票对象
     * @throws ValidationException 验证失败时抛出
     */
    public void validateStock(Stock stock) {
        if (stock == null) {
            throw new ValidationException("股票信息不能为空");
        }

        if (stock.getStockCode() == null || stock.getStockCode().trim().isEmpty()) {
            throw new ValidationException("股票代码不能为空");
        }

        // 验证并设置股票类型
        if (stock.getStockType() == null || stock.getStockType().trim().isEmpty()) {
            stock.setStockType(StockTypeEnum.A_SHARE.getName());
        } else {
            StockTypeEnum typeEnum = StockTypeEnum.fromName(stock.getStockType());
            if (typeEnum == null) {
                throw new ValidationException("无效的股票类型：" + stock.getStockType());
            }
            stock.setStockType(typeEnum.getName());
        }
    }

    /**
     * 标准化股票代码（移除前缀）
     *
     * @param stockCode 原始股票代码
     * @return 标准化后的股票代码
     */
    public String normalizeStockCode(String stockCode) {
        if (stockCode == null) {
            return null;
        }
        return stockCode.replaceAll("^(sh|sz|hk|gb_)", "").toUpperCase();
    }

    /**
     * 获取所有股票列表
     *
     * @return 股票列表
     */
    public List<Stock> getAllStocks() {
        List<icu.iseenu.stock.entity.Stock> stockEntities = stockMapper.selectList(null);
        return StockConverter.toDomainList(stockEntities);
    }

    /**
     * 保存或更新股票（允许覆盖已存在的股票）
     *
     * @param stock 股票对象
     * @return 操作结果信息
     * @throws ValidationException 验证失败时抛出
     */
    @Transactional
    public String saveOrUpdateStock(Stock stock) {
        // 验证股票信息
        validateStock(stock);

        // 标准化股票代码
        stock.setStockCode(normalizeStockCode(stock.getStockCode()));

        // 转换为数据库实体
        icu.iseenu.stock.entity.Stock stockEntity = StockConverter.toEntity(stock);

        // 查找是否已存在该股票代码
        icu.iseenu.stock.entity.Stock existingStock = stockMapper.selectByStockCode(stock.getStockCode());
        
        if (existingStock != null) {
            // 更新
            stockEntity.setId(existingStock.getId());
            stockMapper.updateById(stockEntity);
            return "股票信息更新成功";
        } else {
            // 新增
            stockMapper.insert(stockEntity);
            return "股票信息添加成功";
        }
    }

    /**
     * 新增股票（严格模式：不允许重复的股票代码）
     *
     * @param stock 股票对象
     * @return 操作结果信息
     * @throws ValidationException 股票代码已存在或验证失败
     */
    @Transactional
    public String addStock(Stock stock) {
        // 验证股票信息
        validateStock(stock);

        // 标准化股票代码
        stock.setStockCode(normalizeStockCode(stock.getStockCode()));

        // 检查股票代码是否已存在
        if (stockMapper.existsByStockCode(stock.getStockCode())) {
            throw new ValidationException("股票代码已存在：" + stock.getStockCode() +
                    "，请使用更新接口或先删除原有记录");
        }

        // 转换为数据库实体并插入
        icu.iseenu.stock.entity.Stock stockEntity = StockConverter.toEntity(stock);
        stockMapper.insert(stockEntity);

        return "股票信息添加成功";
    }

    /**
     * 批量保存或更新股票
     *
     * @param stocks 股票列表
     * @return 操作结果信息
     * @throws ValidationException 验证失败时抛出
     */
    @Transactional
    public String saveBatchStocks(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            throw new ValidationException("股票列表不能为空");
        }

        // 验证所有股票
        for (Stock stock : stocks) {
            try {
                validateStock(stock);
            } catch (ValidationException e) {
                throw new ValidationException(e.getMessage() + ": " + stock);
            }
            // 标准化股票代码
            stock.setStockCode(normalizeStockCode(stock.getStockCode()));
        }

        int updatedCount = 0;
        int addedCount = 0;

        // 逐个处理股票
        for (Stock stock : stocks) {
            icu.iseenu.stock.entity.Stock stockEntity = StockConverter.toEntity(stock);
            
            // 查找是否已存在
            icu.iseenu.stock.entity.Stock existingStock = stockMapper.selectByStockCode(stock.getStockCode());
            
            if (existingStock != null) {
                // 更新
                stockEntity.setId(existingStock.getId());
                stockMapper.updateById(stockEntity);
                updatedCount++;
            } else {
                // 新增
                stockMapper.insert(stockEntity);
                addedCount++;
            }
        }

        return String.format("批量保存成功，更新 %d 只，新增 %d 只，共 %d 只股票",
                updatedCount, addedCount, updatedCount + addedCount);
    }

    /**
     * 根据股票代码查找股票
     *
     * @param stockCode 股票代码
     * @return 股票对象，不存在则返回null
     */
    public Stock findByStockCode(String stockCode) {
        icu.iseenu.stock.entity.Stock stockEntity = stockMapper.selectByStockCode(stockCode);
        return StockConverter.toDomain(stockEntity);
    }

    /**
     * 更新股票信息
     *
     * @param stockCode 股票代码
     * @param stock 新的股票信息
     * @return 操作结果信息
     * @throws ValidationException 股票不存在或验证失败
     * @throws ResourceNotFoundException 股票不存在
     */
    @Transactional
    public String updateStock(String stockCode, Stock stock) {
        // 验证股票信息
        validateStock(stock);

        // 查找股票是否存在
        icu.iseenu.stock.entity.Stock existingStock = stockMapper.selectByStockCode(stockCode);
        if (existingStock == null) {
            throw new ResourceNotFoundException("股票不存在：" + stockCode);
        }

        // 更新
        icu.iseenu.stock.entity.Stock stockEntity = StockConverter.toEntity(stock);
        stockEntity.setId(existingStock.getId());
        stockEntity.setStockCode(stockCode); // 确保股票代码一致
        stockMapper.updateById(stockEntity);
        
        return "股票信息更新成功";
    }

    /**
     * 删除股票
     *
     * @param stockCode 股票代码
     * @return 操作结果信息
     * @throws ResourceNotFoundException 股票不存在
     */
    @Transactional
    public String deleteStock(String stockCode) {
        // 检查股票是否存在
        icu.iseenu.stock.entity.Stock existingStock = stockMapper.selectByStockCode(stockCode);
        if (existingStock == null) {
            throw new ResourceNotFoundException("股票不存在：" + stockCode);
        }

        // 逻辑删除（MyBatis-Plus会自动处理）
        stockMapper.deleteById(existingStock.getId());
        return "股票信息删除成功";
    }

    /**
     * 检查股票是否存在
     *
     * @param stockCode 股票代码
     * @return 是否存在
     */
    public boolean exists(String stockCode) {
        return stockMapper.existsByStockCode(stockCode);
    }
}
