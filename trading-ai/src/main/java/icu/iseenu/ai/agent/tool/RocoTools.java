package icu.iseenu.ai.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.Tool;
import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.model.Product;
import icu.iseenu.roco.service.HtmlGenerator;
import icu.iseenu.roco.service.ImageUploadService;
import icu.iseenu.roco.service.ScreenshotService;
import icu.iseenu.roco.util.HttpClientUtil;
import icu.iseenu.roco.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RocoTools {

    private final AppConfig config;

    public RocoTools(AppConfig config) {
        this.config = config;
    }

    @Tool("查询洛克王国远行商人当前售卖的商品信息")
    public String queryRocoMerchant() {
        log.info("调用 queryRocoMerchant 接口");

        if (!config.hasRocomApiKey()) {
            return "洛克王国API密钥未配置，请在 application.yml 中设置 roco.rocom-api-key";
        }

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-API-Key", config.getRocomApiKey());

            String response = HttpClientUtil.sendGet(AppConfig.GAME_API_URL, headers);
            JsonNode jsonResponse = HttpClientUtil.parseJson(response);

            int code = jsonResponse.has("code") ? jsonResponse.get("code").asInt() : -1;
            if (code != 0) {
                String message = jsonResponse.has("message")
                        ? jsonResponse.get("message").asText() : "未知错误";
                return "查询洛克王国远行商人失败: " + message;
            }

            JsonNode data = jsonResponse.has("data") ? jsonResponse.get("data") : null;
            if (data == null || data.isNull()) {
                return "洛克王国远行商人暂无数据";
            }

            var templateData = TimeUtil.processDataForTemplate(data);
            List<Product> products = templateData.getProducts();

            var roundInfo = templateData.getRoundInfo();
            StringBuilder result = new StringBuilder();
            result.append("🏪 **洛克王国远行商人**\n");
            result.append("当前轮次: 第").append(roundInfo.getCurrent())
                    .append("/").append(roundInfo.getTotal()).append("轮\n");
            result.append("剩余时间: ").append(roundInfo.getCountdown()).append("\n\n");

            if (products != null && !products.isEmpty()) {
                result.append("**当前售卖商品:**\n");
                for (Product product : products) {
                    result.append("- ").append(product.getName());
                    String timeLabel = product.getTimeLabel();
                    if (timeLabel != null && !timeLabel.isEmpty()) {
                        result.append(" (").append(timeLabel).append(")");
                    }
                    result.append("\n");
                }
            } else {
                result.append("当前暂无商品");
            }

            // 生成截图并上传图床
            String imageUrl = generateScreenshot(templateData);
            if (imageUrl != null) {
                result.append("\n\n![商品详情](").append(imageUrl).append(")");
            }

            return result.toString();
        } catch (Exception e) {
            log.error("查询洛克王国远行商人失败", e);
            return "查询洛克王国远行商人失败: " + e.getMessage();
        }
    }

    private String generateScreenshot(icu.iseenu.roco.model.TemplateData templateData) {
        if (!config.hasImgbbKey()) {
            log.info("未配置 imgbb-key，跳过截图生成");
            return null;
        }

        try {
            String outputDir = System.getProperty("user.dir");
            HtmlGenerator htmlGenerator = new HtmlGenerator(outputDir, AppConfig.TEMP_RENDER_FILE);
            String htmlPath = htmlGenerator.generateHtml(templateData);

            if (htmlPath == null) {
                return null;
            }

            ScreenshotService screenshotService = new ScreenshotService(AppConfig.SCREENSHOT_FILE);
            String screenshotPath = screenshotService.captureScreenshot(htmlPath);

            if (screenshotPath == null) {
                return null;
            }

            ImageUploadService uploadService = new ImageUploadService(config);
            String imageUrl = uploadService.uploadToImgbb(screenshotPath);
            log.info("截图上传成功: {}", imageUrl);
            return imageUrl;
        } catch (Exception e) {
            log.error("生成截图失败", e);
            return null;
        }
    }
}
