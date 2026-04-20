package icu.iseenu.controller;

import icu.iseenu.common.Result;
import icu.iseenu.entity.Stock;
import icu.iseenu.entity.StockMarketData;
import icu.iseenu.service.JsonFileService;
import icu.iseenu.service.StockApiService;
import icu.iseenu.service.StockService;
import icu.iseenu.task.StockDataScheduledTask;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 股票管理控制??
 * 提供股票信息的保存、读取等 RESTful API 接口
 */
@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, maxAge = 3600)
public class StockController {

    private final JsonFileService jsonFileService;
    private final StockApiService stockApiService;
    private final StockDataScheduledTask stockDataScheduledTask;
    private final StockService stockService;

    public StockController(JsonFileService jsonFileService,
                           StockApiService stockApiService,
                           StockDataScheduledTask stockDataScheduledTask,
                           StockService stockService) {
        this.jsonFileService = jsonFileService;
        this.stockApiService = stockApiService;
        this.stockDataScheduledTask = stockDataScheduledTask;
        this.stockService = stockService;
    }

    /**
     * 保存单只股票信息到 JSON 文件（统一保存到 stocks.json）
     *
     * @param stock 股票信息
     * @return 响应结果
     */
    @PostMapping("/save")
    public Result<Stock> saveStock(@RequestBody Stock stock) {
        try {
            String message = stockService.saveOrUpdateStock(stock);
            return Result.success(message, stock);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IOException e) {
            return Result.internalError("保存失败: " + e.getMessage());
        }
    }

    /**
     * 新增股票信息（严格模式：不允许重复的股票代码）
     *
     * @param stock 股票信息
     * @return 响应结果
     */
    @PostMapping("/add")
    public Result<Stock> addStock(@RequestBody Stock stock) {
        try {
            String message = stockService.addStock(stock);
            return Result.success(message, stock);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IOException e) {
            return Result.internalError("保存失败: " + e.getMessage());
        }
    }

    /**
     * 批量保存多只股票信息到同一个 JSON 文件
     *
     * @param stocks 股票列表
     * @return 响应结果
     */
    @PostMapping("/save-batch")
    public Result<List<Stock>> saveBatchStocks(@RequestBody List<Stock> stocks) {
        try {
            String message = stockService.saveBatchStocks(stocks);
            List<Stock> allStocks = stockService.getAllStocks();
            return Result.success(message, allStocks);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IOException e) {
            return Result.internalError("批量保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有股票信息
     *
     * @return 所有股票列表
     */
    @GetMapping("/list")
    public Result<List<Stock>> getAllStocksList() {
        try {
            List<Stock> stocks = stockService.getAllStocks();
            return Result.success("查询成功", stocks);
        } catch (Exception e) {
            return Result.internalError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据股票代码读取股票信息
     *
     * @param stockCode 股票代码
     * @return 股票信息
     */
    @GetMapping("/{stockCode}")
    public Result<Stock> getStockByCode(@PathVariable String stockCode) {
        try {
            Stock stock = stockService.findByStockCode(stockCode);
            if (stock != null) {
                return Result.success("查询成功", stock);
            }
            return Result.notFound("股票不存在：" + stockCode);
        } catch (Exception e) {
            return Result.internalError("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新股票信息
     *
     * @param stockCode 股票代码
     * @param stock     新的股票信息
     * @return 响应结果
     */
    @PutMapping("/{stockCode}")
    public Result<Stock> updateStock(
            @PathVariable String stockCode,
            @RequestBody Stock stock) {
        try {
            String message = stockService.updateStock(stockCode, stock);
            return Result.success(message, stock);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IOException e) {
            return Result.internalError("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除股票信息
     *
     * @param stockCode 股票代码
     * @return 响应结果
     */
    @DeleteMapping("/{stockCode}")
    public Result<Void> deleteStock(@PathVariable String stockCode) {
        try {
            String message = stockService.deleteStock(stockCode);
            return Result.success(message, null);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (IOException e) {
            return Result.internalError("删除失败: " + e.getMessage());
        }
    }

    /**
     * 检查股票是否存在
     *
     * @param stockCode 股票代码
     * @return 是否存在
     */
    @GetMapping("/{stockCode}/exists")
    public Result<Boolean> checkStockExists(@PathVariable String stockCode) {
        try {
            boolean exists = stockService.exists(stockCode);
            return Result.success("查询成功", exists);
        } catch (Exception e) {
            return Result.internalError("查询失败: " + e.getMessage());
        }
    }



    /**
     * 获取所有股票的实时行情数据（包含盈亏计算）
     *
     * @return 股票行情数据列表（含盈亏??
     */
    @GetMapping("/market-data/all")
    public Result<List<StockMarketData>> getAllMarketData() {
        try {
            List<StockMarketData> marketDataList = stockApiService.fetchAllStockMarketDataWithProfit();
            return Result.success("获取成功", marketDataList);
        } catch (IOException e) {
            return Result.internalError("获取行情数据成功失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有股票的实时行情数据和盈亏汇??
     *
     * @return 股票行情数据列表和总盈??
     */
    @GetMapping("/profit-summary")
    public Result<Map<String, Object>> getProfitSummary() {
        try {
            List<StockMarketData> marketDataList = stockApiService.fetchAllStockMarketDataWithProfit();

            // 计算总盈??
            java.math.BigDecimal totalProfitLoss = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalMarketValue = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalCost = java.math.BigDecimal.ZERO;

            for (StockMarketData data : marketDataList) {
                if (data.getProfitLoss() != null) {
                    totalProfitLoss = totalProfitLoss.add(data.getProfitLoss());
                }
                if (data.getMarketValue() != null) {
                    totalMarketValue = totalMarketValue.add(data.getMarketValue());
                }
                if (data.getHoldingCost() != null) {
                    totalCost = totalCost.add(data.getHoldingCost());
                }
            }

            Map<String, Object> summary = new HashMap<>();
            summary.put("stocks", marketDataList);
            summary.put("totalProfitLoss", totalProfitLoss);
            summary.put("totalMarketValue", totalMarketValue);
            summary.put("totalCost", totalCost);
            summary.put("profitLossPercent", totalCost.compareTo(java.math.BigDecimal.ZERO) > 0 ?
                    totalProfitLoss.multiply(new BigDecimal("100")).divide(totalCost, 2, java.math.RoundingMode.HALF_UP) : null);

            return Result.success("获取成功", summary);
        } catch (IOException e) {
            return Result.internalError("获取盈亏数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取单只股票的实时行情数??
     *
     * @param stockCode 股票代码
     * @return 股票行情数据
     */
    @GetMapping("/market-data/{stockCode}")
    public Result<StockMarketData> getSingleMarketData(@PathVariable String stockCode) {
        try {
            StockMarketData marketData = stockApiService.fetchStockMarketData(stockCode);
            if (marketData == null) {
                return Result.notFound("未找到股票行情数据：" + stockCode);
            }
            return Result.success("获取成功", marketData);
        } catch (IOException e) {
            return Result.internalError("获取行情数据成功失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取股票行情数据
     *
     * @param stockCodes 股票代码列表（逗号分隔??
     * @return 股票行情数据列表
     */
    @GetMapping("/market-data/batch")
    public Result<List<StockMarketData>> getBatchMarketData(@RequestParam String stockCodes) {
        try {
            String[] codes = stockCodes.split(",");
            List<String> codeList = new java.util.ArrayList<>();
            for (String code : codes) {
                if (code != null && !code.trim().isEmpty()) {
                    codeList.add(code.trim());
                }
            }

            if (codeList.isEmpty()) {
                return Result.badRequest("股票代码不能为空为空");
            }

            List<StockMarketData> marketDataList = stockApiService.fetchMarketData(codeList);
            return Result.success("获取成功", marketDataList);
        } catch (IOException e) {
            return Result.internalError("获取行情数据成功失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有股票的盈亏概览（股票名称、今日盈亏、总盈亏）
     *
     * @return 股票盈亏概览列表
     */
    @GetMapping("/profit-overview")
    public Result<List<Map<String, Object>>> getProfitOverview() {
        try {
            List<StockMarketData> marketDataList = stockApiService.fetchAllStockMarketDataWithProfit();

            List<Map<String, Object>> overviewList = new ArrayList<>();
            BigDecimal totalTodayProfitLoss = BigDecimal.ZERO;

            // 遍历所有股票，累加今日盈亏
            for (StockMarketData data : marketDataList) {
                Map<String, Object> overview = new HashMap<>();
                overview.put("stockName", data.getName());
                overview.put("todayProfitLoss", data.getTodayProfitLoss());
                overviewList.add(overview);

                // 累加今日盈亏
                if (data.getTodayProfitLoss() != null) {
                    totalTodayProfitLoss = totalTodayProfitLoss.add(data.getTodayProfitLoss());
                }
            }

            // 添加汇总信息（只包含今日盈亏总和??
            Map<String, Object> summary = new HashMap<>();
            summary.put("stockName", "合计");
            summary.put("todayProfitLoss", totalTodayProfitLoss);
            overviewList.add(summary);

            return Result.success("获取成功", overviewList);
        } catch (IOException e) {
            return Result.internalError("获取盈亏概览失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发股票数据获取任务
     *
     * @return 执行结果
     */
    @GetMapping("/task/execute")
    public Result<Map<String, Object>> executeManualTask() {
        try {
            long startTime = System.currentTimeMillis();

            // 执行手动任务
            String result = stockDataScheduledTask.executeManual();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("message", result);
            response.put("duration", duration + "ms");
            response.put("executeTime", java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            return Result.success("手动任务执行成功", response);
        } catch (Exception e) {
            return Result.internalError("手动任务执行失败: " + e.getMessage());
        }
    }
}


