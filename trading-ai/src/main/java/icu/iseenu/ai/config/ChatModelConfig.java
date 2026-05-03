package icu.iseenu.ai.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@Slf4j
public class ChatModelConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name:deepseek-chat}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.timeout:60000}")
    private long timeout;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:4096}")
    private int maxTokens;

    @Bean
    public ChatModel chatModel() {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("⚠️  DeepSeek API Key 未设置 — ChatModel 将以占位模式启动");
        } else {
            log.info("✅ 创建 ChatModel: baseUrl={}, modelName={}", baseUrl, modelName);
        }
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(Duration.ofMillis(timeout))
                .maxTokens(maxTokens)
                .build();
    }
}
