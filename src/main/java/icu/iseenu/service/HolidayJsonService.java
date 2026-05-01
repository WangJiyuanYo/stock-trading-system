package icu.iseenu.service;

import icu.iseenu.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class HolidayJsonService {

    @Value("${app.json.calender.path}")
    private String calenderPath;

    private static final String HOLIDAY_JSON_FILE = "cn_holiday.json";

    /**
     * 清空并写入新的JSON数据到节假日文件
     *
     * @param jsonString JSON格式的字符串
     * @throws ValidationException 参数验证失败
     * @throws IOException 文件操作异常
     */
    public void writeHolidayJson(String jsonString) throws IOException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new ValidationException("JSON字符串不能为空");
        }

        Path path = Paths.get(calenderPath, HOLIDAY_JSON_FILE);
        
        // 确保父目录存在
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // 直接覆盖写入（自动清空原内容）
        try (FileWriter writer = new FileWriter(path.toFile(), false)) {
            writer.write(jsonString);
            writer.flush();
        }
    }

    /**
     * 格式化并写入JSON数据（美化输出）
     *
     * @param jsonString JSON格式的字符串
     * @throws ValidationException 参数验证失败
     * @throws IOException 文件操作异常
     */
    public void writeFormattedHolidayJson(String jsonString) throws IOException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new ValidationException("JSON字符串不能为空");
        }

        Path path = Paths.get(calenderPath, HOLIDAY_JSON_FILE);
        
        // 确保父目录存在
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // 这里可以添加JSON格式化逻辑，如果需要的话
        // 目前直接写入原始字符串
        try (FileWriter writer = new FileWriter(path.toFile(), false)) {
            writer.write(jsonString);
            writer.flush();
        }
    }
}
