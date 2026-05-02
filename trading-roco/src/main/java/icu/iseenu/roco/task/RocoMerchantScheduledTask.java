package icu.iseenu.roco.task;

import icu.iseenu.roco.service.RocoMerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 洛克王国远行商人定时监控任务
 */
@Component
@Slf4j
public class RocoMerchantScheduledTask {

    @Autowired
    private RocoMerchantService rocoMerchantService;

    /**
     * 定时执行监控
     * 每天 8:01, 12:01, 16:01, 20:01 执行（在 8:01-20:30 期间每4小时执行一次）
     * 可以通过配置文件 roco.cron 自定义cron表达式
     */
    @Scheduled(cron = "${roco.cron:0 1 8,12,16,20 * * ?}")
    public void executeMonitor() {
        log.info("⏰ 触发远行商人定时监控任务");
        rocoMerchantService.monitorMerchant();
    }
}
