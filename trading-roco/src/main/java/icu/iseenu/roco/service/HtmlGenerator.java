package icu.iseenu.roco.service;

import icu.iseenu.roco.model.Product;
import icu.iseenu.roco.model.RoundInfo;
import icu.iseenu.roco.model.TemplateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 轻量级HTML生成服务 - 无需模板引擎和浏览器
 * 支持从JAR包资源和文件系统读取模板
 */
public class HtmlGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(HtmlGenerator.class);
    
    private final String outputDir;
    private final String outputFile;
    private final String templatePath;
    
    public HtmlGenerator(String outputDir, String outputFile) {
        this(outputDir, outputFile, "assets/yuanxing-shangren/index.html");
    }
    
    public HtmlGenerator(String outputDir, String outputFile, String templatePath) {
        this.outputDir = outputDir;
        this.outputFile = outputFile;
        this.templatePath = templatePath;
    }
    
    /**
     * 生成HTML文件并返回路径
     */
    public String generateHtml(TemplateData data) throws IOException {
        if (data == null || data.getProductCount() == 0) {
            logger.info("当前无活跃商品，跳过HTML生成");
            return null;
        }
        
        try {
            // 读取模板文件（支持JAR资源和文件系统）
            String templateContent = loadTemplate();
            
            // 替换模板变量
            String renderedHtml = renderTemplate(templateContent, data);
            
            // 写入临时文件
            Path outputPath = Paths.get(outputDir, outputFile);
            
            // 确保输出目录存在
            Files.createDirectories(outputPath.getParent());
            
            try (FileWriter writer = new FileWriter(outputPath.toFile(), StandardCharsets.UTF_8)) {
                writer.write(renderedHtml);
            }
            
            logger.info("✅ HTML生成成功: {}", outputPath.toAbsolutePath());
            return outputPath.toString();
            
        } catch (Exception e) {
            logger.error("❌ HTML生成失败", e);
            throw new IOException("HTML生成失败", e);
        }
    }
    
    /**
     * 加载模板文件（支持JAR包资源和文件系统）
     */
    private String loadTemplate() throws IOException {
        // 首先尝试从classpath加载（JAR包内）
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templatePath);
        
        if (inputStream != null) {
            logger.debug("从JAR资源加载模板: {}", templatePath);
            try (InputStream is = inputStream) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        
        // 如果classpath中没有，尝试从文件系统加载
        Path templateFilePath = Paths.get(templatePath);
        if (Files.exists(templateFilePath)) {
            logger.debug("从文件系统加载模板: {}", templateFilePath.toAbsolutePath());
            return Files.readString(templateFilePath, StandardCharsets.UTF_8);
        }
        
        // 最后尝试相对于当前工作目录
        Path cwdTemplatePath = Paths.get(System.getProperty("user.dir"), templatePath);
        if (Files.exists(cwdTemplatePath)) {
            logger.debug("从工作目录加载模板: {}", cwdTemplatePath.toAbsolutePath());
            return Files.readString(cwdTemplatePath, StandardCharsets.UTF_8);
        }
        
        throw new IOException("找不到模板文件: " + templatePath);
    }
    
    /**
     * 渲染模板，替换变量
     */
    private String renderTemplate(String template, TemplateData data) {
        RoundInfo roundInfo = data.getRoundInfo();
        List<Product> products = data.getProducts();
        
        // 替换基本变量
        String result = template
            .replace("{{title}}", escapeHtml(data.getTitle()))
            .replace("{{subtitle}}", escapeHtml(data.getSubtitle()))
            .replace("{{product_count}}", String.valueOf(data.getProductCount()))
            .replace("{{background}}", data.getBackground())
            .replace("{{_res_path}}", data.getResPath());
        
        // 替换轮次信息
        if (roundInfo != null) {
            String currentRound = roundInfo.getCurrent() != null ? 
                roundInfo.getCurrent().toString() : "未开放";
            result = result
                .replace("{{round_info.current}}", currentRound)
                .replace("{{round_info.total}}", String.valueOf(roundInfo.getTotal()))
                .replace("{{round_info.countdown}}", escapeHtml(roundInfo.getCountdown()));
        }
        
        // 替换titleIcon条件
        if (data.isTitleIcon()) {
            result = result.replace("{% if titleIcon %}", "")
                          .replace("{% else %}", "<!--")
                          .replace("{% endif %}", "-->");
        } else {
            result = result.replace("{% if titleIcon %}", "<!--")
                          .replace("{% else %}", "")
                          .replace("{% endif %}", "-->");
        }
        
        // 生成商品列表HTML
        String productsHtml = generateProductsHtml(products);
        result = result.replace("{% if products and products|length > 0 %}", "")
                      .replace("{% for product in products %}", productsHtml)
                      .replace("{% endfor %}", "")
                      .replace("{% else %}", "<!--")
                      .replace("<div class=\"product-empty\">本轮暂无商品，稍后再来看看。</div>", "")
                      .replace("{% endif %}", "-->");
        
        return result;
    }
    
    /**
     * 生成商品列表HTML
     */
    private String generateProductsHtml(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "";
        }
        
        StringBuilder html = new StringBuilder();
        for (Product product : products) {
            html.append("                <div class=\"product-card\">\n")
                .append("                    <div class=\"product-image-container\">\n")
                .append("                        <img class=\"product-image\" src=\"")
                .append(escapeHtml(product.getImage())).append("\" alt=\"")
                .append(escapeHtml(product.getName())).append("\">\n")
                .append("                    </div>\n")
                .append("                    <div class=\"product-main\">\n")
                .append("                        <div class=\"product-name\">")
                .append(escapeHtml(product.getName())).append("</div>\n")
                .append("                        <div class=\"product-sub\">远行商人当前轮次商品</div>\n")
                .append("                        <div class=\"product-time\">北京时间 ")
                .append(escapeHtml(product.getTimeLabel())).append("</div>\n")
                .append("                    </div>\n")
                .append("                    <div class=\"product-side\">\n")
                .append("                        <div class=\"product-slot\">本轮商品</div>\n")
                .append("                    </div>\n")
                .append("                </div>\n");
        }
        
        return html.toString();
    }
    
    /**
     * 生成CSS样式
     */
    private String generateCss() {
        return "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
               "        body { font-family: 'Microsoft YaHei', sans-serif; background: #ece3d3; padding: 20px; }\n" +
               "        .merchant-page { max-width: 800px; margin: 0 auto; }\n" +
               "        .content-container { background: white; border-radius: 20px; padding: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }\n" +
               "        .header { margin-bottom: 24px; padding-bottom: 20px; border-bottom: 2px solid #eee; }\n" +
               "        .title { font-size: 32px; font-weight: bold; color: #332719; margin-bottom: 8px; }\n" +
               "        .subtitle { font-size: 16px; color: #6b5846; }\n" +
               "        .round-info { display: flex; gap: 12px; margin-top: 12px; }\n" +
               "        .round-badge { background: #ffe6cc; padding: 6px 12px; border-radius: 12px; font-size: 14px; color: #9a5f19; }\n" +
               "        .product-card { background: #f9f6f0; border-radius: 16px; padding: 16px; margin-bottom: 16px; display: flex; align-items: center; gap: 16px; }\n" +
               "        .product-image { width: 80px; height: 80px; object-fit: contain; background: white; border-radius: 12px; padding: 8px; }\n" +
               "        .product-info { flex: 1; }\n" +
               "        .product-name { font-size: 20px; font-weight: bold; color: #332719; margin-bottom: 4px; }\n" +
               "        .product-time { font-size: 14px; color: #6b5846; }\n";
    }
    
    /**
     * 生成头部HTML
     */
    private String generateHeader(TemplateData data) {
        RoundInfo roundInfo = data.getRoundInfo();
        
        StringBuilder html = new StringBuilder();
        html.append("            <div class=\"header\">\n")
            .append("                <div class=\"title\">").append(escapeHtml(data.getTitle())).append("</div>\n")
            .append("                <div class=\"subtitle\">").append(escapeHtml(data.getSubtitle())).append("</div>\n")
            .append("                <div class=\"round-info\">\n");
        
        if (roundInfo != null) {
            html.append("                    <span class=\"round-badge\">第 ")
                .append(roundInfo.getCurrent() != null ? roundInfo.getCurrent() : "未开放")
                .append(" / ").append(roundInfo.getTotal()).append(" 轮</span>\n")
                .append("                    <span class=\"round-badge\">剩余 ").append(escapeHtml(roundInfo.getCountdown())).append("</span>\n");
        }
        
        html.append("                    <span class=\"round-badge\">当前商品数: ").append(data.getProductCount()).append("</span>\n")
            .append("                </div>\n")
            .append("            </div>\n");
        
        return html.toString();
    }
    
    /**
     * 生成商品列表HTML
     */
    private String generateProductList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return "            <div style=\"text-align: center; padding: 40px; color: #666;\">本轮暂无商品，稍后再来看看。</div>\n";
        }
        
        StringBuilder html = new StringBuilder();
        for (Product product : products) {
            html.append("            <div class=\"product-card\">\n")
                .append("                <img class=\"product-image\" src=\"")
                .append(escapeHtml(product.getImage())).append("\" alt=\"")
                .append(escapeHtml(product.getName())).append("\">\n")
                .append("                <div class=\"product-info\">\n")
                .append("                    <div class=\"product-name\">")
                .append(escapeHtml(product.getName())).append("</div>\n")
                .append("                    <div class=\"product-time\">北京时间 ")
                .append(escapeHtml(product.getTimeLabel())).append("</div>\n")
                .append("                </div>\n")
                .append("            </div>\n");
        }
        
        return html.toString();
    }
    
    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
