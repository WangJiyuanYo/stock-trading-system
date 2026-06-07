package icu.iseenu.roco.task;

import icu.iseenu.roco.service.FarmMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 洛克王国家园种植定时监控任务
 * [家园监控已关闭] 取消 @Component 注解，不再注册为 Spring Bean
 */
// @Component
@Slf4j
public class FarmScheduledTask {

    private final FarmMonitorService farmMonitorService;

    public FarmScheduledTask(FarmMonitorService farmMonitorService) {
        this.farmMonitorService = farmMonitorService;
    }

    // [家园监控已关闭] 取消 @Scheduled 注解，不再定时执行
    // @Scheduled(cron = "${roco.farm-cron:0 0 * * * ?}")
    public void executeMonitor() {
        log.info("⏰ 触发家园种植定时监控任务");
        farmMonitorService.monitorFarms();
    }
}
