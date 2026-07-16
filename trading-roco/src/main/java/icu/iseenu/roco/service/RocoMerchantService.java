package icu.iseenu.roco.service;

import com.fasterxml.jackson.databind.JsonNode;
import icu.iseenu.notification.NotificationService;
import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.model.MerchantMonitorResult;
import icu.iseenu.roco.model.Product;
import icu.iseenu.roco.model.TemplateData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 洛克王国远行商人监控服务
 */
@Service
@Slf4j
public class RocoMerchantService {

    private final AppConfig config;
    private final NotificationService notificationService;
    private final MerchantScreenshotAsyncService screenshotAsyncService;

    public RocoMerchantService(AppConfig config,
                               NotificationService notificationService,
                               MerchantScreenshotAsyncService screenshotAsyncService) {
        this.config = config;
        this.notificationService = notificationService;
        this.screenshotAsyncService = screenshotAsyncService;
    }

    /**
     * 执行监控流程
     */
    public MerchantMonitorResult monitorMerchant() {
        log.info("🚀 开始监控洛克王国远行商人...");
        MerchantMonitorResult result = new MerchantMonitorResult();

        try {
            // 验证配置
            if (!config.hasRocomApiKey()) {
                log.error("❌ 未配置 roco.rocom-api-key");
                result.setMessage("ROCOM_API_KEY 未配置");
                return result;
            }

            // 1. 获取游戏数据
            JsonNode rawData = screenshotAsyncService.fetchGameData();
            if (rawData == null) {
                sendErrorNotification("无法获取游戏数据");
                result.setMessage("无法获取远行商人数据");
                return result;
            }
            result.setDataFetched(true);

            // 2. 处理数据
            TemplateData processedData = screenshotAsyncService.processData(rawData);
            result.setMerchantData(processedData);

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
            result.setImageUrl(imageUrl);
            result.setScreenshotUploaded(imageUrl != null && !imageUrl.isBlank());

            // 5. 发送推送通知
            String alertMessage = "📢 远行商人已刷新\n" + pushBody;
            if (imageUrl != null) {
                alertMessage += "\n\n![商品详情](" + imageUrl + ")";
            }
            List<String> enabledChannels = notificationService.getEnabledChannels();
            result.setNotificationChannels(enabledChannels);
            if (!enabledChannels.isEmpty()) {
                notificationService.sendAlert("roco", "洛克王国远行商人", alertMessage);
                result.setNotificationTriggered(true);
            }

            if (result.isFullChainSuccessful()) {
                result.setMessage("远行商人全链路执行成功");
            } else if (!result.isScreenshotUploaded()) {
                result.setMessage("数据获取成功，但截图或图床上传失败；通知已继续执行");
            } else {
                result.setMessage("数据和图片处理成功，但没有可用的通知渠道");
            }

            log.info("✅ 监控流程执行完成");
            return result;

        } catch (Exception e) {
            log.error("❌ 监控流程异常", e);
            result.setMessage("监控流程异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 发送错误通知
     */
    private void sendErrorNotification(String errorMessage) {
        notificationService.sendAlert("roco", "洛克王国监控异常", "⚠️ " + errorMessage);
    }
}
