package icu.iseenu.roco.service;

import com.fasterxml.jackson.databind.JsonNode;
import icu.iseenu.notification.NotificationService;
import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.model.Product;
import icu.iseenu.roco.model.TemplateData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 洛克王国远行商人监控服务
 */
@Service
@Slf4j
public class RocoMerchantService {

    @Autowired
    private AppConfig config;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MerchantScreenshotAsyncService screenshotAsyncService;

    /**
     * 执行监控流程
     */
    public void monitorMerchant() {
        log.info("🚀 开始监控洛克王国远行商人...");

        try {
            // 验证配置
            if (!config.hasRocomApiKey()) {
                log.error("❌ 未配置 roco.rocom-api-key");
                return;
            }

            // 1. 获取游戏数据
            JsonNode rawData = screenshotAsyncService.fetchGameData();
            if (rawData == null) {
                sendErrorNotification("无法获取游戏数据");
                return;
            }

            // 2. 处理数据
            TemplateData processedData = screenshotAsyncService.processData(rawData);

            // 3. 构建推送内容
            List<Product> products = processedData.getProducts();
            String pushBody;
            if (products != null && !products.isEmpty()) {
                String itemNames = products.stream()
                        .map(Product::getName)
                        .collect(Collectors.joining("、"));
                pushBody = "当前售卖: " + itemNames;
            } else {
                pushBody = "当前暂无商品";
            }

            // 4. 生成HTML并截图上传
            String imageUrl = screenshotAsyncService.generateAndUploadScreenshot(processedData);

            // 5. 发送推送通知
            String alertMessage = "📢 远行商人已刷新\n" + pushBody;
            if (imageUrl != null) {
                alertMessage += "\n\n![商品详情](" + imageUrl + ")";
            }
            notificationService.sendAlert("洛克王国远行商人", alertMessage);

            log.info("✅ 监控流程执行完成");

        } catch (Exception e) {
            log.error("❌ 监控流程异常", e);
        }
    }

    /**
     * 发送错误通知
     */
    private void sendErrorNotification(String errorMessage) {
        notificationService.sendAlert("洛克王国监控异常", "⚠️ " + errorMessage);
    }
}
