package icu.iseenu.roco.service;

import icu.iseenu.notification.NotificationService;
import icu.iseenu.roco.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 洛克王国家园种植监控服务
 * 每小时检查作物成熟状态，即将成熟的作物通过通知渠道推送
 */
@Service
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

    private Boolean isPush = false;

    public void monitorFarms() {
        List<String> uids = config.getFarmUids();
        if (uids == null || uids.isEmpty()) {
            log.debug("未配置家园监控UID，跳过");
            return;
        }

        log.info("🌾 开始家园种植监控, UIDs={}, 阈值={}分钟", uids, THRESHOLD_MINUTES);

        for (String uid : uids) {
            if (uid == null || uid.isBlank()) {
                continue;
            }

            try {
                List<FarmService.NearlyRipePlant> plants = farmService.getNearlyRipePlants(uid.trim(), THRESHOLD_MINUTES);
                if (!plants.isEmpty() && isPush) {
                    String plantsText = plants.stream()
                            .map(FarmService.NearlyRipePlant::description)
                            .reduce((a, b) -> a + "\n" + b)
                            .orElse("");
                    String content = "**洛克王国家园作物成熟提醒**\n\nUID: " + uid.trim() + "\n\n" +
                            plantsText +
                            "\n\n---\n⏰ 请及时收获，避免被偷菜！";
                    notificationService.sendAlert("洛克王国家园作物成熟提醒", content);
                    isPush = false;
                    log.info("✅ 已发送成熟提醒, uid={}, 作物数={}", uid, plants.size());
                } else {
                    log.debug("无即将成熟作物, uid={}", uid);
                    isPush = true;
                }
            } catch (Exception e) {
                log.error("家园监控异常, uid={}", uid, e);
            }
        }

        log.info("🌾 家园种植监控完成");
    }
}
