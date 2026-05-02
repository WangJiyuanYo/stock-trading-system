package icu.iseenu.stock.api;

import icu.iseenu.domain.entity.Stock;
import icu.iseenu.domain.entity.StockMarketData;
import icu.iseenu.infra.storage.JsonFileService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 股票行情 API 服务
 * 调用新浪财经 API 获取股票实时行情数据
 */
@Service
public class StockApiService {

    private static final String API_URL = "https://hq.sinajs.cn/list=";
    
    private final HttpClient httpClient;
    private final JsonFileService jsonFileService;

    public StockApiService(JsonFileService jsonFileService) {
        this.jsonFileService = jsonFileService;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     * ??JSON 文件读取所有股票代码并获取行情数据（包含盈亏计算）
     *
     * @return 股票行情数据列表（含盈亏??
     */
    public List<StockMarketData> fetchAllStockMarketDataWithProfit() throws IOException {
        // ??JSON 文件读取所有股票（包含持仓信息??
        List<Stock> allStocks = getAllStocks();
        
        if (allStocks.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取股票代码列表
        List<String> stockCodes = new ArrayList<>();
        for (Stock stock : allStocks) {
            if (stock.getStockCode() != null && !stock.getStockCode().trim().isEmpty()) {
                stockCodes.add(stock.getStockCode());
            }
        }

        // 批量获取行情数据
        List<StockMarketData> marketDataList = fetchMarketData(stockCodes);
        
        // 合并持仓信息并计算盈??
        mergeHoldingInfo(marketDataList, allStocks);
        
        return marketDataList;
    }

    /**
     * 合并持仓信息到行情数据中
     */
    private void mergeHoldingInfo(List<StockMarketData> marketDataList, List<Stock> allStocks) {
        for (StockMarketData marketData : marketDataList) {
            // 移除市场前缀，匹配股票代??
            String pureCode = removeMarketPrefix(marketData.getStockCode());
            
            for (Stock stock : allStocks) {
                if (pureCode.equals(stock.getStockCode())) {
                    // 设置持仓信息
                    marketData.setHoldingQuantity(stock.getHoldingQuantity());
                    marketData.setHoldingPrice(stock.getHoldingPrice());
                    marketData.setHoldingCost(stock.getHoldingCost());
                    break;
                }
            }
        }
    }

    /**
     * 移除股票代码的市场前缀
     */
    private String removeMarketPrefix(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return "";
        }
        
        String code = stockCode.trim();
        
        // 移除 sh、sz、hk、gb_ 等前缀
        if (code.startsWith("sh") || code.startsWith("sz") || code.startsWith("hk")) {
            return code.substring(2);
        }
        if (code.toLowerCase().startsWith("gb_")) {
            return code.substring(3);
        }
        
        return code;
    }

    /**
     * 根据股票代码获取行情数据（包含盈亏计算）
     *
     * @param stockCode 股票代码
     * @return 股票行情数据（含盈亏??
     */
    public StockMarketData fetchStockMarketDataWithProfit(String stockCode) throws IOException {
        StockMarketData marketData = fetchStockMarketData(stockCode);
        
        if (marketData != null) {
            // 获取持仓信息
            List<Stock> allStocks = getAllStocks();
            String pureCode = removeMarketPrefix(marketData.getStockCode());
            
            for (Stock stock : allStocks) {
                if (pureCode.equals(stock.getStockCode())) {
                    marketData.setHoldingQuantity(stock.getHoldingQuantity());
                    marketData.setHoldingPrice(stock.getHoldingPrice());
                    marketData.setHoldingCost(stock.getHoldingCost());
                    break;
                }
            }
        }
        
        return marketData;
    }

    /**
     * 根据股票代码获取行情数据
     *
     * @param stockCode 股票代码
     * @return 股票行情数据
     */
    public StockMarketData fetchStockMarketData(String stockCode) throws IOException {
        List<String> codes = new ArrayList<>();
        codes.add(stockCode);
        
        List<StockMarketData> dataList = fetchMarketData(codes);
        return dataList.isEmpty() ? null : dataList.get(0);
    }

    /**
     * 批量获取股票行情数据
     *
     * @param stockCodes 股票代码列表
     * @return 股票行情数据列表
     */
    public List<StockMarketData> fetchMarketData(List<String> stockCodes) throws IOException {
        if (stockCodes == null || stockCodes.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建股票代码字符串（逗号分隔??
        String codesParam = String.join(",", stockCodes);
        
        // 构建完整??URL（需要根据市场添加前缀??
        String fullUrl = buildStockUrl(codesParam);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Referer", "http://finance.sina.com.cn/")
                    .header("Accept", "*/*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseStockData(response.body(), stockCodes);
            } else {
                throw new IOException("API 请求失败，状态码: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("请求被中断", e);
        }
    }

    /**
     * 构建股票 URL（根据股票代码添加市场前缀）
     */
    private String buildStockUrl(String codesParam) {
        String[] codes = codesParam.split(",");
        StringBuilder formattedCodes = new StringBuilder();
        
        for (int i = 0; i < codes.length; i++) {
            String code = codes[i].trim();
            if (!code.isEmpty()) {
                // 根据股票代码判断市场
                String marketCode = formatStockCode(code);
                if (i > 0) {
                    formattedCodes.append(",");
                }
                formattedCodes.append(marketCode);
            }
        }
        
        return API_URL + formattedCodes.toString();
    }

    /**
     * 格式化股票代码（添加市场前缀??
     * A 股：sh/sz + 6 位代??
     * 港股：hk + 5 位代??
     * 美股：gb_ + 代码（小写）
     */
    private String formatStockCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "";
        }
        
        code = code.trim();
        
        // 判断 A 股市场（6 位数字）
        if (code.matches("\\d{6}")) {
            char firstChar = code.charAt(0);
            // 沪市???? 开??
            if (firstChar == '6' || firstChar == '5') {
                return "sh" + code; // 沪市（包??ETF??
            } else if (firstChar == '9') {
                return "sh" + code; // 沪市
            } else {
                return "sz" + code; // 深市?????? 开头）
            }
        }
        
        // 港股（通常??5 位数字）
        if (code.matches("\\d{5}")) {
            return "hk" + code;
        }
        
        // 美股（字母或 gb_开头）
        if (code.toLowerCase().startsWith("gb_")) {
            return code.toLowerCase();
        }
        
        // 默认当作美股处理
        return "gb_" + code.toLowerCase();
    }

    /**
     * 解析股票数据
     */
    private List<StockMarketData> parseStockData(String responseBody, List<String> stockCodes) {
        List<StockMarketData> resultList = new ArrayList<>();
        
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return resultList;
        }

        // 按行分割响应数据
        String[] lines = responseBody.split("\n");
        
        for (String line : lines) {
            if (line.contains("=")) {
                StockMarketData data = parseSingleStock(line);
                if (data != null) {
                    resultList.add(data);
                }
            }
        }
        
        return resultList;
    }

    /**
     * 解析单只股票数据
     * 格式：var hq_str_sh600000="浦发银行??.55,8.54,8.57,8.58,8.53..."
     */
    private StockMarketData parseSingleStock(String line) {
        try {
            int eqIndex = line.indexOf("=");
            int quoteStart = line.indexOf("\"", eqIndex);
            int quoteEnd = line.lastIndexOf("\"");
            
            if (eqIndex == -1 || quoteStart == -1 || quoteEnd == -1) {
                return null;
            }

            // 提取股票代码（如 sh600000??
            String stockCodeWithPrefix = line.substring(0, eqIndex).replace("var hq_str_", "").trim();
            
            // 提取数据部分
            String dataPart = line.substring(quoteStart + 1, quoteEnd);
            String[] fields = dataPart.split(",");
            
            if (fields.length < 32) {
                return null;
            }

            StockMarketData data = new StockMarketData();
            data.setStockCode(stockCodeWithPrefix);
            data.setName(fields[0]); // 股票名称
            
            // 今日开盘价
            data.setTodayOpen(parseBigDecimal(fields[1]));
            // 昨日收盘??
            data.setYesterdayClose(parseBigDecimal(fields[2]));
            // 当前价格
            data.setCurrentPrice(parseBigDecimal(fields[3]));
            // 今日最高价
            data.setTodayHigh(parseBigDecimal(fields[4]));
            // 今日最低价
            data.setTodayLow(parseBigDecimal(fields[5]));
            // 竞买价（买一价）
            data.setBidPrice(parseBigDecimal(fields[6]));
            // 竞卖价（卖一价）
            data.setAskPrice(parseBigDecimal(fields[7]));
            // 成交的股票数（手??
            data.setVolume(parseLong(fields[8]));
            // 成交金额（元??
            data.setTurnover(parseBigDecimal(fields[9]));
            // 买一申报数量
            data.setBidQty1(parseLong(fields[10]));
            // 买一申报价格
            data.setBidPrice1(parseBigDecimal(fields[11]));
            // 买二申报数量
            data.setBidQty2(parseLong(fields[12]));
            // 买二申报价格
            data.setBidPrice2(parseBigDecimal(fields[13]));
            // 买三申报数量
            data.setBidQty3(parseLong(fields[14]));
            // 买三申报价格
            data.setBidPrice3(parseBigDecimal(fields[15]));
            // 买四申报数量
            data.setBidQty4(parseLong(fields[16]));
            // 买四申报价格
            data.setBidPrice4(parseBigDecimal(fields[17]));
            // 买五申报数量
            data.setBidQty5(parseLong(fields[18]));
            // 买五申报价格
            data.setBidPrice5(parseBigDecimal(fields[19]));
            // 卖一申报数量
            data.setAskQty1(parseLong(fields[20]));
            // 卖一申报价格
            data.setAskPrice1(parseBigDecimal(fields[21]));
            // 卖二申报数量
            data.setAskQty2(parseLong(fields[22]));
            // 卖二申报价格
            data.setAskPrice2(parseBigDecimal(fields[23]));
            // 卖三申报数量
            data.setAskQty3(parseLong(fields[24]));
            // 卖三申报价格
            data.setAskPrice3(parseBigDecimal(fields[25]));
            // 卖四申报数量
            data.setAskQty4(parseLong(fields[26]));
            // 卖四申报价格
            data.setAskPrice4(parseBigDecimal(fields[27]));
            // 卖五申报数量
            data.setAskQty5(parseLong(fields[28]));
            // 卖五申报价格
            data.setAskPrice5(parseBigDecimal(fields[29]));
            // 日期
            data.setDate(fields[30]);
            // 时间
            data.setTime(fields[31]);
            
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 辅助方法：解??BigDecimal
     */
    private java.math.BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.trim().isEmpty() || "0".equals(value.trim())) {
                return null;
            }
            return new java.math.BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 辅助方法：解??Long
     */
    private Long parseLong(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ??JSON 文件读取所有股票的辅助方法
     */
    @SuppressWarnings("unchecked")
    private List<Stock> getAllStocks() throws IOException {
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
        return new ArrayList<>();
    }
}
