package icu.iseenu.controller;


import icu.iseenu.agent.assistant.HolidayAssistant;
import icu.iseenu.agent.assistant.WriteJsonFileAssistant;
import lombok.extern.slf4j.Slf4j;
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

    private static final String DEFAULT_EXPLANATION = ".json";

    private final HolidayAssistant holidayAssistant;

    private final WriteJsonFileAssistant writeJsonFileAssistant;

    public AiController(HolidayAssistant holidayAssistant,
                        WriteJsonFileAssistant writeJsonFileAssistant) {
        this.holidayAssistant = holidayAssistant;
        this.writeJsonFileAssistant = writeJsonFileAssistant;
    }


    @GetMapping("/fetch-holiday")
    public String fetchHoliday(@RequestParam String year) {
        if ("".equals(year) || Integer.parseInt(year) < 2025) {
            return "请输入正确的年份";
        }
        String fetchHoliday = holidayAssistant.fetchHoliday(year);
        return writeJsonFileAssistant.writJsonFiles(fetchHoliday, calenderPath, HOLIDAY_JSON_FILE + year, DEFAULT_EXPLANATION);
    }
}
