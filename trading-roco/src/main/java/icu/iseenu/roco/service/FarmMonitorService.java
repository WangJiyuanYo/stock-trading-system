package icu.iseenu.roco.service;

import icu.iseenu.notification.NotificationService;
import icu.iseenu.roco.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// [家园监控已关闭] 取消 @Service 注解，不再注册为 Spring Bean
// /**
//  * 洛克王国家园种植监控服务
//  * 每小时检查作物成熟状态，即将成熟的作物通过通知渠道推送
//  */
// @Service
@Slf4j
public class FarmMonitorService {

    private static final int THRESHOLD_MINUTES = 60;

    private final AppConfig config;
    private final FarmService farmService;
    private final NotificationService notificationService;

    public FarmMonitorService(AppConfig config, FarmService farmService,
                              NotificationService notificationService) {
        this.config = config;
        this.farmService = farmService;
        this.notificationService = notificationService;
    }

    /** 记录每个UID上次推送的作物指纹，用于避免同一批作物重复通知 */
    private final Map<String, String> lastNotifiedFingerprint = new HashMap<>();

    public void monitorFarms() {
        List<String> uids = config.getFarmUids();
        if (uids == null || uids.isEmpty()) {
            log.debug("未配置家园监控UID，跳过");
            return;
        }

        // 清理已从配置中移除的UID，防止map无限增长
        lastNotifiedFingerprint.keySet().retainAll(uids);

        log.info("开始家园种植监控, UIDs={}, 阈值={}分钟", uids, THRESHOLD_MINUTES);

        for (String uid : uids) {
            if (uid == null || uid.isBlank()) {
                continue;
            }

            try {
                List<FarmService.NearlyRipePlant> plants = farmService.getNearlyRipePlants(uid.trim(), THRESHOLD_MINUTES);
                if (!plants.isEmpty()) {
                    String currentFingerprint = plants.stream()
                            .map(FarmService.NearlyRipePlant::uniqueKey)
                            .sorted()
                            .collect(Collectors.joining(","));

                    String previousFingerprint = lastNotifiedFingerprint.get(uid.trim());
                    if (!currentFingerprint.equals(previousFingerprint)) {
                        String plantsText = plants.stream()
                                .map(FarmService.NearlyRipePlant::description)
                                .reduce((a, b) -> a + "\n" + b)
                                .orElse("");
                        String content = "**洛克王国家园作物成熟提醒**\n\nUID: " + uid.trim() + "\n\n" +
                                plantsText +
                                "\n\n---\n请及时收获，避免被偷菜！";
                        notificationService.sendAlert("洛克王国家园作物成熟提醒", content);
                        lastNotifiedFingerprint.put(uid.trim(), currentFingerprint);
                        log.info("已发送成熟提醒, uid={}, 作物数={}", uid, plants.size());
                    } else {
                        log.debug("作物成熟状态未变化，跳过推送, uid={}", uid);
                    }
                } else {
                    lastNotifiedFingerprint.remove(uid.trim());
                    log.debug("无即将成熟作物, uid={}", uid);
                }
            } catch (Exception e) {
                log.error("家园监控异常, uid={}", uid, e);
            }
        }

        log.info("家园种植监控完成");
    }
}
