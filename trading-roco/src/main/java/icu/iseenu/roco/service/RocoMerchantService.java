package icu.iseenu.roco.service;

import com.fasterxml.jackson.databind.JsonNode;
import icu.iseenu.notification.NotificationService;
import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.model.Product;
import icu.iseenu.roco.model.TemplateData;
import icu.iseenu.roco.util.HttpClientUtil;
import icu.iseenu.roco.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            JsonNode rawData = fetchGameData();
            if (rawData == null) {
                sendErrorNotification("无法获取游戏数据");
                return;
            }

            // 2. 处理数据
            TemplateData processedData = TimeUtil.processDataForTemplate(rawData);

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
            String imageUrl = null;
            if (processedData.getProductCount() > 0) {
                try {
                    // 使用当前工作目录作为输出目录
                    String outputDir = System.getProperty("user.dir");
                    HtmlGenerator htmlGenerator = new HtmlGenerator(
                            outputDir,
                            AppConfig.TEMP_RENDER_FILE
                    );
                    String htmlPath = htmlGenerator.generateHtml(processedData);

                    if (htmlPath != null) {
                        // 截图
                        ScreenshotService screenshotService = new ScreenshotService(
                                AppConfig.SCREENSHOT_FILE
                        );
                        String screenshotPath = screenshotService.captureScreenshot(htmlPath);

                        // 上传到图床
                        if (screenshotPath != null && config.hasImgbbKey()) {
                            ImageUploadService uploadService = new ImageUploadService(config);
                            imageUrl = uploadService.uploadToImgbb(screenshotPath);
                            log.info("✅ 图片上传成功: {}", imageUrl);
                        }
                    }

                } catch (IOException e) {
                    log.error("❌ HTML生成或截图失败", e);
                }
            }

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
     * 获取游戏数据
     */
    private JsonNode fetchGameData() {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-API-Key", config.getRocomApiKey());

            String response = HttpClientUtil.sendGet(
                    AppConfig.GAME_API_URL,
                    headers
            );

            JsonNode jsonResponse = HttpClientUtil.parseJson(response);

            // 检查响应码
            int code = jsonResponse.has("code") ? jsonResponse.get("code").asInt() : -1;
            if (code != 0) {
                String message = jsonResponse.has("message") ?
                        jsonResponse.get("message").asText() : "未知错误";
                log.error("API返回错误: {}", message);
                return null;
            }

            // 返回data字段
            return jsonResponse.has("data") ? jsonResponse.get("data") : null;

        } catch (IOException e) {
            log.error("❌ 请求游戏API失败", e);
            return null;
        }
    }

    /**
     * 发送错误通知
     */
    private void sendErrorNotification(String errorMessage) {
        notificationService.sendAlert("洛克王国监控异常", "⚠️ " + errorMessage);
    }
}
