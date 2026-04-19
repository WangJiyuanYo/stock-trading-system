package icu.iseenu.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易日判断工具类
 * 用于判断当前日期是否为 A 股交易日
 */
@Component
public class TradingDayUtil {

    private static final Logger log = LoggerFactory.getLogger(TradingDayUtil.class);

    private static final String HOLIDAY_API_URL = "https://timor.tech/api/holiday/";

    private final WebClient webClient;

    // 缓存节假日数据，避免频繁调用 API
    private Map<String, Boolean> holidayCache = new HashMap<>();

    private final ObjectMapper objectMapper;

    @Value("${app.json.calender.path}")
    private String calenderPath;

    public TradingDayUtil(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(HOLIDAY_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 判断是否为交易日
     *
     * @return true-是交易日，false-非交易日
     */
    public boolean isTradingDay() {
        return isTradingDay(LocalDate.now());
    }

    /**
     * 判断指定日期是否为交易日
     *
     * @param date 指定日期
     * @return true-是交易日，false-非交易日
     */
    public boolean isTradingDay(LocalDate date) {
        // 1. 排除周末
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            log.info("今日是周末，非交易日");
            return false;
        }

        // 2. 排除法定节假日（简化版，可根据需要扩展）
//        if (isHoliday(date)) {
//            log.info("今日是节假日，非交易日");
//            return false;
//        }
        try {
            return checkIsChineseHoliday(date);
        } catch (IOException e) {
            log.error("调用错误");
        }

        // 3. 检查是否在交易时间内（可选）
        // 如果需要在非交易时间也执行，可以注释掉这部分
//        if (!isWithinTradingHours()) {
//            log.info("当前时间不在交易时段内");
//            return false;
//        }

//        log.info("今日是交易日：{}", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//        return true;
        return false;
    }

    /**
     * 判断是否为节假日（调用 timor.tech API）
     * API 文档：https://timor.tech/api/holiday/
     *
     * @param date 日期
     * @return true-是节假日，false-不是节假日
     */
    private boolean isHoliday(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 1. 先查缓存
        if (holidayCache.containsKey(dateStr)) {
            Boolean isHoliday = holidayCache.get(dateStr);
            log.debug("命中缓存：{} 是节假日={}", dateStr, isHoliday);
            return isHoliday;
        }

        try {
            // 2. 调用 API 查询
            String response = webClient.get()
                    .uri("info/{date}", dateStr)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("节假日 API 响应：{}", response);

            // 3. 解析响应
            // API 返回格式：{"code":0,"message":"ok","date":{"...","type":"work","name":"工作日"}}
            // type: "work"=工作日，"rest"=休息日
            boolean isHoliday = response != null && response.contains("\"type\":\"rest\"");

            // 4. 存入缓存
            holidayCache.put(dateStr, isHoliday);

            if (isHoliday) {
                // 尝试获取节日名称
                String festivalName = extractFestivalName(response);
                log.info("今日是节假日：{} ({})", dateStr, festivalName);
            }

            return isHoliday;

        } catch (Exception e) {
            log.error("调用节假日 API 失败：{}", e.getMessage());
            // API 调用失败时，默认使用周末判断
            log.warn("使用备用方案：仅根据周末判断");
            return false; // 假设不是节假日
        }
    }

    /**
     * 从响应中提取节日名称
     */
    private String extractFestivalName(String response) {
        if (response == null || !response.contains("\"name\"")) {
            return "未知节假日";
        }

        int nameIndex = response.indexOf("\"name\"");
        int startQuote = response.indexOf("\"", nameIndex + 7);
        int endQuote = response.indexOf("\"", startQuote + 1);

        if (startQuote > 0 && endQuote > startQuote) {
            return response.substring(startQuote + 1, endQuote);
        }

        return "未知节假日";
    }

    /**
     * 判断当前时间是否在交易时段内
     * A 股交易时间：
     * - 上午 9:30 - 11:30
     * - 下午 13:00 - 15:00
     *
     * @return true-在交易时段内，false-不在交易时段内
     */
    private boolean isWithinTradingHours() {
        LocalTime now = LocalTime.now();

        // 上午交易时段 9:30 - 11:30
        boolean morningSession = !now.isBefore(LocalTime.of(9, 30)) &&
                !now.isAfter(LocalTime.of(11, 30));

        // 下午交易时段 13:00 - 15:00
        boolean afternoonSession = !now.isBefore(LocalTime.of(13, 0)) &&
                !now.isAfter(LocalTime.of(15, 0));

        return morningSession || afternoonSession;
    }

    /**
     * 获取下一个交易日
     *
     * @return 下一个交易日日期
     */
    public LocalDate getNextTradingDay() {
        LocalDate nextDay = LocalDate.now().plusDays(1);
        while (!isTradingDay(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    /**
     * 获取上一个交易日
     *
     * @return 上一个交易日日期
     */
    public LocalDate getPreviousTradingDay() {
        LocalDate prevDay = LocalDate.now().minusDays(1);
        while (!isTradingDay(prevDay)) {
            prevDay = prevDay.minusDays(1);
        }
        return prevDay;
    }

    //date eg.2024-01-01
    private boolean checkIsChineseHoliday(LocalDate date) throws IOException {
        int month = date.getMonth().getValue();
        int day = date.getDayOfMonth();

        if (checkFileExits()) {
            // 读取JSON文件
            String jsonContent = new String(Files.readAllBytes(Paths.get(calenderPath + "/cn_holiday.json")));

            // 解析JSON
            Map<String, List<Integer>> holidayData = objectMapper.readValue(
                    jsonContent,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, List.class)
            );

            // 检查该月是否有节假日记录
            String monthKey = String.valueOf(month);
            if (holidayData.containsKey(monthKey)) {
                List<Integer> holidayDays = (List<Integer>) holidayData.get(monthKey);
                // 检查当天是否在节假日列表中
                if (holidayDays.contains(day)) {
                    log.info("今日是节假日（本地数据）：{}-{}", month, day);
                    return true;
                }
            }
        } else {
            log.warn("节假日文件不存在，跳过本地节假日检查");
        }
        return false;
    }

    private boolean checkFileExits() {
        return Files.exists(Paths.get(calenderPath, "cn_holiday.json"));
    }

}
