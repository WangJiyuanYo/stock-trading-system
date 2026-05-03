package icu.iseenu.application.controller;

import icu.iseenu.ai.agent.assistant.HolidayAssistant;
import icu.iseenu.ai.agent.assistant.WriteJsonFileAssistant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AiController {

    @Value("${app.json.calender.path}")
    private String calenderPath;

    private static final String HOLIDAY_JSON_FILE = "cn_holiday_";
    private static final String JSON_EXTENSION = ".json";

    private HolidayAssistant holidayAssistant;
    private WriteJsonFileAssistant writeJsonFileAssistant;

    @Autowired(required = false)
    public void setHolidayAssistant(HolidayAssistant holidayAssistant) {
        this.holidayAssistant = holidayAssistant;
    }

    @Autowired(required = false)
    public void setWriteJsonFileAssistant(WriteJsonFileAssistant writeJsonFileAssistant) {
        this.writeJsonFileAssistant = writeJsonFileAssistant;
    }

    @GetMapping("/fetch-holiday")
    public String fetchHoliday(@RequestParam String year) {
        if (holidayAssistant == null || writeJsonFileAssistant == null) {
            return "AI 服务未配置，请设置 DEEPSEEK_API_KEY 环境变量后重启";
        }
        if (year == null || year.isEmpty() || Integer.parseInt(year) < 2025) {
            return "请输入正确的年份";
        }
        String fetchHoliday = holidayAssistant.fetchHoliday(year);
        return writeJsonFileAssistant.writJsonFiles(fetchHoliday, calenderPath,
                HOLIDAY_JSON_FILE + year, JSON_EXTENSION);
    }
}
