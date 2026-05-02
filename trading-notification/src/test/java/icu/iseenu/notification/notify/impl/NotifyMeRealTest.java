package icu.iseenu.notification.notify.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * NotifyMe 真实推送测试类
 * 需要配置真实的 UUID 才能发送通知
 * 
 * 使用方法：
 * 1. 在 NotifyMe App 中获取你的 UUID
 * 2. 修改下面的 TEST_UUID 常量
 * 3. 运行 main 方法
 */
public class NotifyMeRealTest {

    // TODO: 替换为你的真实 UUID
    private static final String TEST_UUID = "YOUR_UUID_HERE";
    private static final String BASE_URL = "https://notifyme-server.wzn556.top/?";

    public static void main(String[] args) {
        System.out.println("========== NotifyMe 真实推送测试 ==========\n");

        // 检查 UUID 是否配置
        if ("YOUR_UUID_HERE".equals(TEST_UUID)) {
            System.out.println("⚠️  请先配置 TEST_UUID 常量！");
            System.out.println("1. 打开 NotifyMe App");
            System.out.println("2. 在设置中找到你的 UUID");
            System.out.println("3. 修改代码中的 TEST_UUID 常量");
            return;
        }

        try {
            // 测试1: 发送简单消息
            testSimpleMessage();

            // 等待2秒，避免频繁请求
            Thread.sleep(2000);

            // 测试2: 发送股票日报（模拟真实场景）
            testStockReport();

            System.out.println("\n========== 测试完成 ==========");
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试1: 发送简单消息
     */
    private static void testSimpleMessage() throws Exception {
        System.out.println("【测试1】发送简单消息");

        String title = "测试消息";
        String body = "这是一条来自 NotifyMeNotifier 的测试消息！";

        sendNotification(title, body, "TEST", false);
    }

    /**
     * 测试2: 发送股票日报（模拟真实场景）
     */
    private static void testStockReport() throws Exception {
        System.out.println("\n【测试2】发送股票日报");

        String title = "📈 股票日报 - 今日盈利 123.45 元";
        
        StringBuilder body = new StringBuilder();
        body.append("## 📊 今日持仓概览\n\n");
        body.append("**持仓股票数**: 5 只\n");
        body.append("**今日盈亏**: ✅ 123.45 元\n\n");
        body.append("## 📝 个股详情\n\n");
        body.append("贵州茅台: ✅ 50.00 元\n\n");
        body.append("宁德时代: ❌ -20.50 元\n\n");
        body.append("比亚迪: ✅ 30.25 元\n\n");
        body.append("五粮液: ✅ 25.80 元\n\n");
        body.append("中国平安: ❌ -12.10 元\n\n");
        body.append("---\n");
        body.append("*推送时间*: 2026-05-01 15:01:00");

        sendNotification(title, body.toString(), "STOCK", false);
    }

    /**
     * 发送通知（使用原生 HTTP 客户端）
     */
    private static void sendNotification(String title, String body, String group, boolean bigText) throws Exception {
        // 构建参数
        Map<String, String> params = new HashMap<>();
        params.put("uuid", TEST_UUID);
        params.put("title", title);
        params.put("body", body);
        params.put("group", group);
        params.put("bigText", String.valueOf(bigText));

        // 构建 URL
        String url = buildUrlWithParams(BASE_URL, params);
        
        System.out.println("请求 URL: " + url);
        System.out.println("URL 长度: " + url.length());

        // 发送 GET 请求
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        System.out.println("响应码: " + responseCode);

        // 读取响应
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        String responseBody = response.toString();
        System.out.println("响应内容: " + responseBody);

        // 判断是否成功
        if (responseBody.contains("\"isSuccess\":true")) {
            System.out.println("✓ 推送成功！");
        } else {
            System.out.println("✗ 推送失败");
        }

        conn.disconnect();
    }

    /**
     * 构建带参数的 URL
     */
    private static String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl);
        
        if (!baseUrl.endsWith("?")) {
            url.append("?");
        }
        
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                url.append("&");
            }
            try {
                String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                url.append(key).append("=").append(value);
                first = false;
            } catch (Exception e) {
                System.err.println("URL 编码失败: " + e.getMessage());
            }
        }
        
        return url.toString();
    }
}
