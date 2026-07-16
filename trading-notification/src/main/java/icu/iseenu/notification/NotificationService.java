package icu.iseenu.notification;

import icu.iseenu.notification.channel.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务
 * 负责管理和发送多渠道通知
 */
@Service
public class NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    private final List<NotificationChannel> channels;

    /**
     * 构造器注入所有通知渠道
     */
    @Autowired
    public NotificationService(List<NotificationChannel> allChannels) {
        this.channels = allChannels.stream()
                .filter(NotificationChannel::isEnabled)
                .collect(Collectors.toList());
        
        log.info("初始化通知服务，启用的渠道: {}", 
                channels.stream().map(NotificationChannel::getName).collect(Collectors.toList()));
    }
    
    /**
     * 发送通知到所有启用的渠道
     * @param title 标题
     * @param content 内容
     */
    public void sendAlert(String title, String content) {
        sendAlert("default", title, content);
    }

    /**
     * 按业务场景发送通知到所有启用的渠道。
     * NotifyMe 会根据场景选择接收人，其他渠道保持原有行为。
     *
     * @param scene 通知场景，如 stock、roco
     * @param title 标题
     * @param content 内容
     */
    public void sendAlert(String scene, String title, String content) {
        if (channels.isEmpty()) {
            log.warn("没有启用任何通知渠道");
            return;
        }

        channels.forEach(channel -> {
            try {
                channel.send(scene, title, content);
            } catch (Exception e) {
                // 记录错误，但不中断其他渠道的发送
                log.error("场景 {} 的渠道 {} 发送失败", scene, channel.getName(), e);
            }
        });
    }
    
    /**
     * 获取所有启用的渠道名称
     * @return 渠道名称列表
     */
    public List<String> getEnabledChannels() {
        return channels.stream()
                .map(NotificationChannel::getName)
                .collect(Collectors.toList());
    }
}
