package icu.iseenu.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用配置属性
 * 统一管理 application.yml 中的 app.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    /**
     * JSON 存储配置
     */
    private JsonStorage json = new JsonStorage();
    
    /**
     * RAG 配置
     */
    private Rag rag = new Rag();
    
    @Data
    public static class JsonStorage {
        /**
         * 股票数据存储路径
         */
        private String path = "./data/json";
        
        /**
         * 节假日数据路径
         */
        private String calenderPath = "./data/calender";
    }
    
    @Data
    public static class Rag {
        /**
         * 是否自动加载数据到向量库
         */
        private boolean autoLoad = true;
    }
}
