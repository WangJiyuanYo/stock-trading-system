package icu.iseenu.application.config;

import dev.langchain4j.model.chat.ChatModel;
import icu.iseenu.ai.agent.supervisor.SupervisorAgents;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AiStatusLogger {

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private Environment env;

    @PostConstruct
    public void report() {
        // 1. 检查关键属性的实际解析值
        String apiKey = env.getProperty("langchain4j.open-ai.chat-model.api-key", "<未设置>");
        String baseUrl = env.getProperty("langchain4j.open-ai.chat-model.base-url", "<未设置>");
        String modelName = env.getProperty("langchain4j.open-ai.chat-model.model-name", "<未设置>");

        log.info("=== AI 配置诊断 ===");
        log.info("langchain4j.open-ai.chat-model.api-key: {}", maskKey(apiKey));
        log.info("langchain4j.open-ai.chat-model.base-url: {}", baseUrl);
        log.info("langchain4j.open-ai.chat-model.model-name: {}", modelName);

        // 2. 查找所有 ChatModel 类型的 Bean
        Map<String, ChatModel> chatModels = ctx.getBeansOfType(ChatModel.class);
        if (!chatModels.isEmpty()) {
            chatModels.forEach((name, bean) ->
                    log.info("✅ ChatModel Bean 找到: name={}, class={}", name, bean.getClass().getName()));
        } else {
            log.warn("❌ 没有找到任何 ChatModel 类型的 Bean");

            // 3. 尝试列出 langchain4j 相关的 Bean 帮助排查
            String[] langchain4jBeans = ctx.getBeanDefinitionNames();
            log.info("容器中与 'chat'/'model' 相关的 Bean:");
            for (String name : langchain4jBeans) {
                if (name.toLowerCase().contains("chat") || name.toLowerCase().contains("model")) {
                    log.info("  - {}", name);
                }
            }
        }

        // 4. SupervisorAgents
        Map<String, SupervisorAgents> agents = ctx.getBeansOfType(SupervisorAgents.class);
        log.info("SupervisorAgents Bean: {}", agents.isEmpty() ? "未加载" : "已加载 -> " + agents.keySet());
        log.info("===================");
    }

    private static String maskKey(String key) {
        if (key == null || key.length() <= 8) return key;
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }
}
