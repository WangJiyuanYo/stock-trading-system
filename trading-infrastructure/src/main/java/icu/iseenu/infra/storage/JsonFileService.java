package icu.iseenu.infra.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JSON 文件操作服务
 * 提供 JSON 文件的读取和写入功能
 */
@Service
public class JsonFileService {

    @Value("${app.json.storage.path:./data/json}")
    private String storagePath;

    private final ObjectMapper objectMapper;

    public JsonFileService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        // 确保存储目录存在
        try {
            Path path = Paths.get(storagePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("创建 JSON 存储目录失败", e);
        }
    }

    /**
     * 将对象保存为 JSON 文件
     *
     * @param filename 文件名（不包含路径）
     * @param data     要保存的数据对象
     * @throws IOException 写入失败时抛出异??
     */
    public <T> void saveJson(String filename, T data) throws IOException {
        if (!filename.endsWith(".json")) {
            filename = filename + ".json";
        }
        
        Path filePath = Paths.get(storagePath, filename);
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        String json = writer.writeValueAsString(data);
        
        Files.writeString(filePath, json);
    }

    /**
     * ??JSON 文件读取数据并转换为指定类型
     *
     * @param filename 文件??
     * @param clazz    目标类型
     * @return 转换后的对象
     * @throws IOException 读取失败时抛出异??
     */
    public <T> T readJson(String filename, Class<T> clazz) throws IOException {
        if (!filename.endsWith(".json")) {
            filename = filename + ".json";
        }
        
        Path filePath = Paths.get(storagePath, filename);
        
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在：" + filename);
        }
        
        String json = Files.readString(filePath);
        return objectMapper.readValue(json, clazz);
    }

    /**
     * 检??JSON 文件是否存在
     *
     * @param filename 文件??
     * @return 文件是否存在
     */
    public boolean exists(String filename) {
        if (!filename.endsWith(".json")) {
            filename = filename + ".json";
        }
        return Files.exists(Paths.get(storagePath, filename));
    }

    /**
     * 删除 JSON 文件
     *
     * @param filename 文件??
     * @return 是否删除成功
     */
    public boolean delete(String filename) {
        if (!filename.endsWith(".json")) {
            filename = filename + ".json";
        }
        try {
            return Files.deleteIfExists(Paths.get(storagePath, filename));
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败", e);
        }
    }
}
