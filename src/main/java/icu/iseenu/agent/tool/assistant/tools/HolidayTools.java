package icu.iseenu.agent.tool.assistant.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class HolidayTools {

    @Value("${app.json.calender.path}")
    private String calenderPath;

    private static final String FILE_NAME = "cn_holiday.json";

    private final ObjectMapper objectMapper;

    private final ChatModel model;

    public HolidayTools(ObjectMapper objectMapper, ChatModel chatModel) {

        this.objectMapper = objectMapper;
        this.model = chatModel;
    }

    @Tool("判断一个给定的日期是否是中国的法定节假日，根据返回结果，最终回答仅输出 true 或者false")
    public boolean isHoliday(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        int month = date.getMonth().getValue();
        int day = date.getDayOfMonth();

        if (checkFileExits()) {
            try {
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
            } catch (IOException e) {
                log.error("IO异常，文件不存在");
            }
        } else {
            log.warn("节假日文件不存在，跳过本地节假日检查");
        }
        return false;
    }


    private boolean checkFileExits() {
        return Files.exists(Paths.get(calenderPath, FILE_NAME));
    }

    @Tool("校验JSON数据格式")
    public boolean checkDataFormat(String data) {
        log.info("校验数据格式：{}", data);
        return data != null && !data.isEmpty() && data.charAt(0) == '{' && data.charAt(data.length() - 1) == '}';
    }

    @Tool("获取指定年份的节假日")
    public String getHolidayOfYear(String year) {
        String prompt = String.format("""
                你是一个搜索助手，阅读网页内容然后将节假日信息转换为JSON文件，最终答案必须是**JSON格式如下：{"月份":[几号]}**,不要输出任何其他文字、标点、空格或解释
                通过www.gov.cn这个网站搜索**国务院办公厅关于%s年部分节假日安排的通知**
                """, year);
        String chat = model.chat(prompt);
        log.info("获取指定年份的节假日：{}", chat);
        return chat;
    }
}
