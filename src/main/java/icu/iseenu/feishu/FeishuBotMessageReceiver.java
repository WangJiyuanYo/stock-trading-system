package icu.iseenu.feishu;


import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.lark.oapi.ws.Client;
import icu.iseenu.service.FeishuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class FeishuBotMessageReceiver {

    @Value("${feishu.app-id}")
    private String appId; // app_id, required, 应用ID

    @Value("${feishu.app-secret}")
    private String appSecret; // app_secret, required, 应用密钥

    @Autowired
    private FeishuService feishuService;

    // 用于消息去重的缓存,key为messageId,value为处理时间戳
    private static final Set<String> PROCESSED_MESSAGES = ConcurrentHashMap.newKeySet();
    private static final long MESSAGE_CACHE_EXPIRE_MS = 300000; // 5分钟过期

    @Bean
    public Client feishuClient() {
        EventDispatcher eventHandler = EventDispatcher.newBuilder(appId, appSecret)
                .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                    @Override
                    public void handle(P2MessageReceiveV1 event) throws Exception {
                        String messageId = event.getEvent().getMessage().getMessageId();

                        // 幂等性检查：如果消息已处理过，直接返回
                        if (PROCESSED_MESSAGES.contains(messageId)) {
                            log.warn("Duplicate message detected, messageId: {}", messageId);
                            return;
                        }

                        log.info("[ onP2MessageReceiveV1 access ], messageId: {}, data: {}",
                                messageId, Jsons.DEFAULT.toJson(event.getEvent()));

                        // 标记消息为已处理
                        PROCESSED_MESSAGES.add(messageId);

                        try {
                            feishuService.resolveEvent(event.getEvent().getMessage().getContent());
                        } catch (Exception e) {
                            log.error("Error processing message: {}", e.getMessage(), e);
                        }
                    }
                })
                .build();

        Client client = new Client.Builder(appId, appSecret)
                .eventHandler(eventHandler)
                .build();

        // 异步启动长连接，避免阻塞Spring容器初始化
        startClientAsync(client);

        // 启动定时清理任务，防止内存泄漏
        startMessageCacheCleanup();

        return client;
    }

    @Async
    public void startClientAsync(Client client) {
        try {
            log.info("Starting Feishu long connection client...");
            client.start();
        } catch (Exception e) {
            log.error("Failed to start Feishu client: {}", e.getMessage(), e);
        }
    }

    /**
     * 启动定时清理任务，定期清理过期的消息ID，防止内存泄漏
     */
    @Async
    public void startMessageCacheCleanup() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // 每分钟清理一次
                    log.debug("Cleaning up expired message cache, current size: {}", PROCESSED_MESSAGES.size());
                    // 实际项目中应该使用带过期时间的缓存如Caffeine或Guava Cache
                    // 这里简化处理，当缓存过大时清空（生产环境建议使用TTL缓存）
                    if (PROCESSED_MESSAGES.size() > 10000) {
                        PROCESSED_MESSAGES.clear();
                        log.info("Message cache cleared due to size limit");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "feishu-message-cache-cleanup").start();
    }

}