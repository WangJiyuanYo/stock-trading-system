package icu.iseenu.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ChatMemoryConfig {
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        // 为每个 memoryId 返回一个独立的新 ChatMemory 实例
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId) // 设置会话ID以实现隔离
                .maxMessages(20) // 设置保留的最大消息数量
                .build();
    }
}
