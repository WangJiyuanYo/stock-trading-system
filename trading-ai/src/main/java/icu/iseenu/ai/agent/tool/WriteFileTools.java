package icu.iseenu.ai.agent.tool;


import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@ConditionalOnBean(ChatModel.class)  // 只有存在 ChatModel 时才启用
public class WriteFileTools {


    @Tool("向文件中写入内容工具")
    public boolean writFiles(String input, String pathEnv, String fileName, String extension) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("文件内容不能为空");
        }

        // 确保扩展名以 . 开??
        if (extension != null && !extension.isEmpty() && !extension.startsWith(".")) {
            extension = "." + extension;
        }

        log.info("向文件写入内容工具开始执??path:{},file:{},extension:{},input:{}", pathEnv, fileName, extension, input);
        Path path = Paths.get(pathEnv, fileName + extension);

        // 确保父目录存??
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 直接覆盖写入（自动清空原内容??
        try (FileWriter writer = new FileWriter(path.toFile(), false)) {
            writer.write(input);
            writer.flush();
            log.info("文件写入成功");
            return true;
        } catch (IOException e) {
            log.error("文件写入失败");
            return false;
        }
    }
}
