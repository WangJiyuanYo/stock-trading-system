package icu.iseenu.feishu;

import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.lark.oapi.ws.Client;
import icu.iseenu.feishu.service.FeishuService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "feishu.app-id")
@Slf4j
public class FeishuBotMessageReceiver {

    @Value("${feishu.app-id:}")
    private String appId;

    @Value("${feishu.app-secret:}")
    private String appSecret;

    @Autowired
    private FeishuService feishuService;

    private Client wsClient;

    private static final Set<String> PROCESSED_MESSAGES = ConcurrentHashMap.newKeySet();
    private static final long MESSAGE_CACHE_EXPIRE_MS = 300_000;

    private static final ScheduledExecutorService cacheCleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "feishu-cache-cleanup");
                t.setDaemon(true);
                return t;
            });

    @PostConstruct
    public void init() {
        EventDispatcher eventHandler = EventDispatcher.newBuilder(appId, appSecret)
                .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                    @Override
                    public void handle(P2MessageReceiveV1 event) {
                        String messageId = event.getEvent().getMessage().getMessageId();

                        if (!PROCESSED_MESSAGES.add(messageId)) {
                            log.debug("Duplicate message ignored: {}", messageId);
                            return;
                        }

                        String createTime = event.getEvent().getMessage().getCreateTime();
                        long timestamp = Long.parseLong(createTime);
                        if (System.currentTimeMillis() - timestamp > MESSAGE_CACHE_EXPIRE_MS) {
                            PROCESSED_MESSAGES.remove(messageId);
                            log.info("过期消息丢弃: {}", Jsons.DEFAULT.toJson(event.getEvent()));
                            return;
                        }

                        log.info("收到飞书消息: messageId={}, data={}",
                                messageId, Jsons.DEFAULT.toJson(event.getEvent()));

                        // 异步处理，快速返回避免飞书重试
                        String chatId = event.getEvent().getMessage().getChatId();
                        String senderId = event.getEvent().getSender().getSenderId().getOpenId();
                        String content = event.getEvent().getMessage().getContent();

                        CompletableFuture.runAsync(() -> {
                            try {
                                feishuService.resolveEvent(chatId, senderId, content);
                            } catch (Exception e) {
                                log.error("消息处理异常: {}", e.getMessage(), e);
                            }
                        });
                    }
                })
                .build();

        wsClient = new Client.Builder(appId, appSecret)
                .eventHandler(eventHandler)
                .build();

        startClientAsync();
        startMessageCacheCleanup();
    }

    private void startClientAsync() {
        new Thread(() -> {
            try {
                log.info("启动飞书长连接客户端...");
                wsClient.start();
            } catch (Exception e) {
                log.error("飞书客户端启动失败: {}", e.getMessage(), e);
            }
        }, "feishu-ws-client").start();
    }

    private void startMessageCacheCleanup() {
        cacheCleaner.scheduleAtFixedRate(() -> {
            if (PROCESSED_MESSAGES.size() > 10000) {
                PROCESSED_MESSAGES.clear();
                log.info("消息去重缓存已清理");
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
}
