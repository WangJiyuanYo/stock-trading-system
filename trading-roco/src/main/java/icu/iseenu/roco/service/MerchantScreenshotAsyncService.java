package icu.iseenu.roco.service;

import com.fasterxml.jackson.databind.JsonNode;
import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.model.TemplateData;
import icu.iseenu.roco.util.HttpClientUtil;
import icu.iseenu.roco.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 远行商人截图异步服务 — 供飞书查询和定时任务复用
 */
@Service
@Slf4j
public class MerchantScreenshotAsyncService {

    private final AppConfig config;

    public MerchantScreenshotAsyncService(AppConfig config) {
        this.config = config;
    }

    /**
     * 获取游戏原始数据
     */
    public JsonNode fetchGameData() {
        if (!config.hasRocomApiKey()) {
            log.warn("rocom-api-key 未配置");
            return null;
        }
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-API-Key", config.getRocomApiKey());
            String response = HttpClientUtil.sendGet(AppConfig.GAME_API_URL, headers);
            JsonNode jsonResponse = HttpClientUtil.parseJson(response);
            int code = jsonResponse.has("code") ? jsonResponse.get("code").asInt() : -1;
            if (code != 0) {
                log.error("API返回错误: {}", jsonResponse.has("message") ? jsonResponse.get("message").asText() : "未知");
                return null;
            }
            return jsonResponse.has("data") ? jsonResponse.get("data") : null;
        } catch (Exception e) {
            log.error("请求游戏API失败", e);
            return null;
        }
    }

    /**
     * 处理原始数据为模板数据
     */
    public TemplateData processData(JsonNode rawData) {
        return TimeUtil.processDataForTemplate(rawData);
    }

    /**
     * 生成截图并上传图床，返回图片URL
     */
    public String generateAndUploadScreenshot(TemplateData templateData) {
        if (templateData == null || templateData.getProductCount() == 0) {
            return null;
        }
        if (!config.hasImgbbKey()) {
            log.info("imgbb-key 未配置，跳过截图");
            return null;
        }
        try {
            Path workDir = Files.createTempDirectory("roco-merchant-");
            HtmlGenerator htmlGenerator = new HtmlGenerator(workDir.toString(), AppConfig.TEMP_RENDER_FILE);
            String htmlPath = htmlGenerator.generateHtml(templateData);
            if (htmlPath == null) {
                return null;
            }

            String screenshotFile = workDir.resolve(AppConfig.SCREENSHOT_FILE).toString();
            ScreenshotService screenshotService = new ScreenshotService(screenshotFile);
            String screenshotPath = screenshotService.captureScreenshot(htmlPath);
            if (screenshotPath == null) {
                return null;
            }

            ImageUploadService uploadService = new ImageUploadService(config);
            String imageUrl = uploadService.uploadToImgbb(screenshotPath);
            log.info("截图上传成功: {}", imageUrl);
            return imageUrl;
        } catch (Exception e) {
            log.error("截图生成失败", e);
            return null;
        }
    }
}
