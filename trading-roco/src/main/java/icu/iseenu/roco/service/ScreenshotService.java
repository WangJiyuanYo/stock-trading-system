package icu.iseenu.roco.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ScreenshotType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 浏览器截图服务 - 使用Playwright进行HTML页面截图
 */
public class ScreenshotService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotService.class);
    
    private final String screenshotFile;
    private Playwright playwright;
    private Browser browser;
    
    public ScreenshotService(String screenshotFile) {
        this.screenshotFile = screenshotFile;
    }
    
    /**
     * 对HTML文件进行截图
     * @param tempHtmlPath HTML文件路径
     * @return 截图文件路径，失败返回null
     */
    public String captureScreenshot(String tempHtmlPath) {
        if (tempHtmlPath == null || tempHtmlPath.isEmpty()) {
            logger.warn("HTML文件路径为空，跳过截图");
            return null;
        }
        
        try {
            logger.info("正在启动浏览器进行截图...");
            
            // 启动Playwright
            playwright = Playwright.create();
            
            // 启动Chromium浏览器
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)); // 无头模式
            
            // 创建新页面
            Page page = browser.newPage(new Browser.NewPageOptions()
                    .setViewportSize(900, 1200));
            
            // 加载本地HTML文件
            String fileUrl = "file:///" + new File(tempHtmlPath).getAbsolutePath().replace("\\", "/");
            logger.info("正在加载HTML文件: {}", fileUrl);
            page.navigate(fileUrl);
            
            // 等待字体加载完成
            page.evaluate("() => document.fonts.ready");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
            // 定位原版HTML的包裹容器并截图
            Locator dataRegion = page.locator(".merchant-page");
            
            // 确保元素可见
            dataRegion.waitFor(new Locator.WaitForOptions()
                    .setTimeout(10000));
            
            // 截取JPEG图片，质量90
            Path screenshotPath = Paths.get(screenshotFile);
            dataRegion.screenshot(new Locator.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setType(ScreenshotType.JPEG)
                    .setQuality(90));
            
            logger.info("✅ 截图成功: {}", screenshotPath.toAbsolutePath());
            
            // 关闭浏览器
            close();
            
            return screenshotPath.toString();
            
        } catch (Exception e) {
            logger.error("❌ 截图失败", e);
            close();
            return null;
        }
    }
    
    /**
     * 关闭浏览器资源
     */
    public void close() {
        try {
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            logger.warn("关闭浏览器资源失败", e);
        }
    }
}
