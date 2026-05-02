package icu.iseenu.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 交易日判断工具类
 * 用于判断当前日期是否为 A 股交易日
 */
@Component
public class TradingDayUtil {

    private static final Logger log = LoggerFactory.getLogger(TradingDayUtil.class);

    private final ObjectMapper objectMapper;

    @Value("${app.json.calender.path}")
    private String calenderPath;

    public TradingDayUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

        // 2. 检查是否为法定节假日
        try {
            boolean isHoliday = checkIsChineseHoliday(date);
            if (isHoliday) {
                log.info("今日是节假日，非交易日");
                return false;
            }
        } catch (IOException e) {
            log.error("检查节假日失败", e);
        }

        log.info("今日是交易日：{}", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return true;
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
