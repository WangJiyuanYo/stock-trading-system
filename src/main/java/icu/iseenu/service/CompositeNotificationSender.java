package icu.iseenu.service;


import icu.iseenu.notify.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CompositeNotificationSender {

    private final List<NotificationSender> delegates;

    // Spring 会注入容器中所有 NotificationSender 的实现类
    @Autowired
    public CompositeNotificationSender(List<NotificationSender> allSenders,
                                       @Value("${notification.enabled-channels}") String enabledChannelsStr) {
        // 将逗号分隔的字符串转换为List
        List<String> enabledChannelNames = Arrays.asList(enabledChannelsStr.split(","));
        
        log.info("配置文件中启用的渠道: {}", enabledChannelNames);
        log.info("Spring容器中找到的通知实现: {}", 
                allSenders.stream().map(NotificationSender::name).collect(Collectors.toList()));
        
        // 根据配置的渠道名称过滤出需要的实现
        this.delegates = allSenders.stream()
                .filter(sender -> enabledChannelNames.contains(sender.name()))
                .collect(Collectors.toList());
        
        log.info("初始化复合通知发送器，启用的渠道: {}", enabledChannelNames);
        log.info("实际加载的通知渠道: {}", delegates.stream().map(NotificationSender::name).collect(Collectors.toList()));
    }
    
    public void send(String title, String content) {
        if (delegates.isEmpty()) {
            throw new IllegalStateException("没有启用任何通知渠道");
        }

        delegates.forEach(sender -> {
            try {
                sender.send(title, content);
            } catch (Exception e) {
                // 记录错误，但不中断其他渠道的发送
                log.error("渠道 {} 发送失败", sender.name(), e);
            }
        });
    }
}
