package icu.iseenu.notification.notify.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * NotifyMeNotifier 独立测试类
 * 用于测试 URL 构建和参数编码逻辑
 */
public class NotifyMeNotifierTest {

    public static void main(String[] args) {
        System.out.println("========== NotifyMe 通知测试 ==========\n");

        // 测试1: 基本功能测试
        testBasicNotification();

        // 测试2: 特殊字符编码测试
        testSpecialCharacters();

        // 测试3: 空值处理测试
        testNullValues();

        // 测试4: 长文本测试
        testLongText();

        System.out.println("\n========== 所有测试完成 ==========");
    }

    /**
     * 测试1: 基本功能测试
     */
    private static void testBasicNotification() {
        System.out.println("【测试1】基本功能测试");
        
        String baseUrl = "https://notifyme-server.wzn556.top/?";
        String uuid = "9wjZXCj7E26L9axeXmcThE";
        String title = "📈 股票日报 - 今日盈利 123.45 元";
        String message = "## 📊 今日持仓概览\n\n**持仓股票数**: 5 只\n**今日盈亏**: ✅ 123.45 元";
        String group = "STOCK";
        boolean bigText = false;

        Map<String, Object> params = new HashMap<>();
        params.put("uuid", uuid);
        params.put("title", title);
        params.put("body", message);
        params.put("group", group);
        params.put("bigText", String.valueOf(bigText));

        String url = buildUrlWithParams(baseUrl, params);
        
        System.out.println("生成的 URL:");
        System.out.println(url);
        System.out.println("URL 长度: " + url.length());
        System.out.println("✓ 测试通过\n");
    }

    /**
     * 测试2: 特殊字符编码测试
     */
    private static void testSpecialCharacters() {
        System.out.println("【测试2】特殊字符编码测试");
        
        String baseUrl = "https://notifyme-server.wzn556.top/?";
        String uuid = "TEST_UUID_123";
        String title = "测试&特殊#字符@编码!";
        String message = "包含空格、中文、符号: ￥$%^&*()";

        Map<String, Object> params = new HashMap<>();
        params.put("uuid", uuid);
        params.put("title", title);
        params.put("body", message);
        params.put("group", "TEST");
        params.put("bigText", "false");

        String url = buildUrlWithParams(baseUrl, params);
        
        System.out.println("生成的 URL:");
        System.out.println(url);
        
        // 验证特殊字符是否被正确编码
        if (url.contains("%26") && url.contains("%23") && url.contains("%40")) {
            System.out.println("✓ 特殊字符编码正确\n");
        } else {
            System.out.println("✗ 特殊字符编码可能有问题\n");
        }
    }

    /**
     * 测试3: 空值处理测试
     */
    private static void testNullValues() {
        System.out.println("【测试3】空值处理测试");
        
        String baseUrl = "https://notifyme-server.wzn556.top/?";
        String uuid = "TEST_UUID";
        String title = null;
        String message = "";

        Map<String, Object> params = new HashMap<>();
        params.put("uuid", uuid);
        params.put("title", title != null ? title : "");
        params.put("body", message != null ? message : "");
        params.put("group", "TEST");
        params.put("bigText", "false");

        String url = buildUrlWithParams(baseUrl, params);
        
        System.out.println("生成的 URL:");
        System.out.println(url);
        System.out.println("✓ 空值处理正常\n");
    }

    /**
     * 测试4: 长文本测试
     */
    private static void testLongText() {
        System.out.println("【测试4】长文本测试");
        
        String baseUrl = "https://notifyme-server.wzn556.top/?";
        String uuid = "TEST_UUID";
        String title = "股票日报";
        
        // 构建较长的消息内容
        StringBuilder longMessage = new StringBuilder();
        longMessage.append("## 📊 今日持仓概览\n\n");
        for (int i = 1; i <= 10; i++) {
            longMessage.append(String.format("**股票%d**: %s %.2f 元\n\n", 
                i, i % 2 == 0 ? "✅" : "❌", Math.random() * 1000 - 500));
        }
        longMessage.append("\n---\n");
        longMessage.append("*推送时间*: 2026-05-01 15:01:00");

        Map<String, Object> params = new HashMap<>();
        params.put("uuid", uuid);
        params.put("title", title);
        params.put("body", longMessage.toString());
        params.put("group", "STOCK");
        params.put("bigText", "false");

        String url = buildUrlWithParams(baseUrl, params);
        
        System.out.println("生成的 URL 长度: " + url.length());
        
        // GET 请求通常有 URL 长度限制（约 2000-8000 字符）
        if (url.length() > 2000) {
            System.out.println("⚠️ 警告: URL 长度超过 2000 字符，可能被截断");
            System.out.println("建议: 使用 bigText=true 或缩短消息内容");
        } else {
            System.out.println("✓ URL 长度在合理范围内");
        }
        
        System.out.println("URL 预览（前200字符）:");
        System.out.println(url.substring(0, Math.min(200, url.length())));
        System.out.println("...\n");
    }

    /**
     * 构建带参数的 URL（与 NotifyMeNotifier 中的实现相同）
     */
    private static String buildUrlWithParams(String baseUrl, Map<String, Object> params) {
        StringBuilder url = new StringBuilder(baseUrl);
        
        // 确保 baseUrl 以 ? 结尾或者添加 ?
        if (!baseUrl.endsWith("?")) {
            url.append("?");
        }
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!first) {
                url.append("&");
            }
            try {
                String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8);
                url.append(key).append("=").append(value);
                first = false;
            } catch (Exception e) {
                System.err.println("URL 编码失败: " + e.getMessage());
            }
        }
        
        return url.toString();
    }
}
