package icu.iseenu.stock.service;

import icu.iseenu.domain.entity.Stock;
import icu.iseenu.domain.enums.StockTypeEnum;
import icu.iseenu.common.exception.ResourceNotFoundException;
import icu.iseenu.common.exception.ValidationException;
import icu.iseenu.infra.storage.JsonFileService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 股票业务服务
 * 提供股票数据的校验、保存等业务逻辑
 */
@Service
public class StockService {

    private final JsonFileService jsonFileService;

    public StockService(JsonFileService jsonFileService) {
        this.jsonFileService = jsonFileService;
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
     * @throws IOException 读取文件失败
     */
    @SuppressWarnings("unchecked")
    public List<Stock> getAllStocks() throws IOException {
        try {
            if (jsonFileService.exists("stocks")) {
                Object obj = jsonFileService.readJson("stocks", Object.class);
                if (obj instanceof List) {
                    List<?> list = (List<?>) obj;
                    List<Stock> stocks = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof java.util.Map) {
                            @SuppressWarnings("rawtypes")
                            java.util.Map map = (java.util.Map) item;
                            Stock stock = new Stock();
                            stock.setStockType((String) map.get("stockType"));
                            stock.setStockCode((String) map.get("stockCode"));

                            // 读取持仓数量
                            Object qtyObj = map.get("holdingQuantity");
                            if (qtyObj instanceof Number) {
                                stock.setHoldingQuantity(((Number) qtyObj).longValue());
                            }

                            // 读取持仓价格
                            Object priceObj = map.get("holdingPrice");
                            if (priceObj instanceof Number) {
                                stock.setHoldingPrice(new java.math.BigDecimal(priceObj.toString()));
                            }

                            stocks.add(stock);
                        }
                    }
                    return stocks;
                }
            }
        } catch (IOException e) {
            throw new IOException("读取股票数据失败: " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 保存或更新股票（允许覆盖已存在的股票）
     *
     * @param stock 股票对象
     * @return 操作结果信息
     * @throws ValidationException 验证失败时抛出
     * @throws IOException 保存失败
     */
    public String saveOrUpdateStock(Stock stock) throws IOException {
        // 验证股票信息
        validateStock(stock);

        // 标准化股票代码
        stock.setStockCode(normalizeStockCode(stock.getStockCode()));

        // 获取所有股票
        List<Stock> allStocks = getAllStocks();

        // 查找是否已存在该股票代码
        boolean exists = false;
        for (int i = 0; i < allStocks.size(); i++) {
            if (allStocks.get(i).getStockCode().equals(stock.getStockCode())) {
                allStocks.set(i, stock);
                exists = true;
                break;
            }
        }

        if (!exists) {
            allStocks.add(stock);
        }

        // 保存到JSON文件
        jsonFileService.saveJson("stocks", allStocks);

        return exists ? "股票信息更新成功" : "股票信息添加成功";
    }

    /**
     * 新增股票（严格模式：不允许重复的股票代码）
     *
     * @param stock 股票对象
     * @return 操作结果信息
     * @throws ValidationException 股票代码已存在或验证失败
     * @throws IOException 保存失败
     */
    public String addStock(Stock stock) throws IOException {
        // 验证股票信息
        validateStock(stock);

        // 标准化股票代码
        stock.setStockCode(normalizeStockCode(stock.getStockCode()));

        // 获取所有股票
        List<Stock> allStocks = getAllStocks();

        // 检查股票代码是否已存在
        for (Stock existingStock : allStocks) {
            if (existingStock.getStockCode().equals(stock.getStockCode())) {
                throw new ValidationException("股票代码已存在：" + stock.getStockCode() +
                        "，请使用更新接口或先删除原有记录");
            }
        }

        // 添加到列表
        allStocks.add(stock);

        // 保存到JSON文件
        jsonFileService.saveJson("stocks", allStocks);

        return "股票信息添加成功";
    }

    /**
     * 批量保存或更新股票
     *
     * @param stocks 股票列表
     * @return 操作结果信息
     * @throws ValidationException 验证失败时抛出
     * @throws IOException 保存失败
     */
    public String saveBatchStocks(List<Stock> stocks) throws IOException {
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

        // 获取所有股票
        List<Stock> allStocks = getAllStocks();

        int updatedCount = 0;
        int addedCount = 0;

        // 合并股票列表
        for (Stock newStock : stocks) {
            boolean exists = false;
            for (int i = 0; i < allStocks.size(); i++) {
                if (allStocks.get(i).getStockCode().equals(newStock.getStockCode())) {
                    allStocks.set(i, newStock);
                    updatedCount++;
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allStocks.add(newStock);
                addedCount++;
            }
        }

        // 保存到JSON文件
        jsonFileService.saveJson("stocks", allStocks);

        return String.format("批量保存成功，更新 %d 只，新增 %d 只，共 %d 只股票",
                updatedCount, addedCount, allStocks.size());
    }

    /**
     * 根据股票代码查找股票
     *
     * @param stockCode 股票代码
     * @return 股票对象，不存在则返回null
     * @throws IOException 读取失败
     */
    public Stock findByStockCode(String stockCode) throws IOException {
        List<Stock> allStocks = getAllStocks();
        for (Stock stock : allStocks) {
            if (stock.getStockCode().equals(stockCode)) {
                return stock;
            }
        }
        return null;
    }

    /**
     * 更新股票信息
     *
     * @param stockCode 股票代码
     * @param stock 新的股票信息
     * @return 操作结果信息
     * @throws ValidationException 股票不存在或验证失败
     * @throws ResourceNotFoundException 股票不存在
     * @throws IOException 保存失败
     */
    public String updateStock(String stockCode, Stock stock) throws IOException {
        // 验证股票信息
        validateStock(stock);

        List<Stock> allStocks = getAllStocks();

        // 查找并更新
        for (int i = 0; i < allStocks.size(); i++) {
            if (allStocks.get(i).getStockCode().equals(stockCode)) {
                stock.setStockCode(stockCode); // 确保股票代码一致
                allStocks.set(i, stock);
                jsonFileService.saveJson("stocks", allStocks);
                return "股票信息更新成功";
            }
        }

        throw new ResourceNotFoundException("股票不存在：" + stockCode);
    }

    /**
     * 删除股票
     *
     * @param stockCode 股票代码
     * @return 操作结果信息
     * @throws ResourceNotFoundException 股票不存在
     * @throws IOException 保存失败
     */
    public String deleteStock(String stockCode) throws IOException {
        List<Stock> allStocks = getAllStocks();

        // 查找并删除
        boolean removed = false;
        for (int i = 0; i < allStocks.size(); i++) {
            if (allStocks.get(i).getStockCode().equals(stockCode)) {
                allStocks.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            throw new ResourceNotFoundException("股票不存在：" + stockCode);
        }

        // 保存更新后的列表
        jsonFileService.saveJson("stocks", allStocks);
        return "股票信息删除成功";
    }

    /**
     * 检查股票是否存在
     *
     * @param stockCode 股票代码
     * @return 是否存在
     * @throws IOException 读取失败
     */
    public boolean exists(String stockCode) throws IOException {
        List<Stock> allStocks = getAllStocks();
        for (Stock stock : allStocks) {
            if (stock.getStockCode().equals(stockCode)) {
                return true;
            }
        }
        return false;
    }
}
