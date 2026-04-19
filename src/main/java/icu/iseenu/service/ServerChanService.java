package icu.iseenu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Server 酱微信推送服务
 * 用于将股票盈亏等消息推送到微信
 */
@Service
public class ServerChanService {

    private static final Logger log = LoggerFactory.getLogger(ServerChanService.class);
    
    private static final String SERVER_CHAN_URL = "https://sctapi.ftqq.com/";

    @Value("${serverchan.sendkey:}")
    private String sendkey;

    private final WebClient webClient;

    public ServerChanService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(SERVER_CHAN_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    /**
     * 发送微信消息
     *
     * @param title      消息标题（必填）
     * @param desp       消息内容（可选，支持 Markdown）
     * @return 是否发送成功
     */
    public boolean sendWechatMessage(String title, String desp) {
        if (sendkey == null || sendkey.trim().isEmpty()) {
            log.warn("Server 酱 SendKey 未配置，无法发送消息");
            return false;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("title", title);
            params.put("desp", desp != null ? desp : "");

            // 正确的 URL 格式：https://sctapi.ftqq.com/{sendkey}.send
            String response = webClient.post()
                    .uri(sendkey + ".send")
                    .bodyValue(buildFormData(params))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Server 酱推送响应：{}", response);
            
            // 检查响应是否成功
            return response != null && (response.contains("\"code\":0") || response.contains("\"errno\":0"));
            
        } catch (Exception e) {
            log.error("Server 酱推送失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送股票盈亏日报
     *
     * @param stockCount       股票数量
     * @param totalProfitLoss  总盈亏
     * @param stockDetails     股票详情列表
     * @return 是否发送成功
     */
    public boolean sendStockDailyReport(int stockCount, double totalProfitLoss, String stockDetails) {
        String title = buildTitle(totalProfitLoss);
        String desp = buildDesp(stockCount, totalProfitLoss, stockDetails);
        
        return sendWechatMessage(title, desp);
    }

    /**
     * 构建消息标题
     */
    private String buildTitle(double totalProfitLoss) {
        String emoji;
        if (totalProfitLoss > 0) {
            emoji = "📈";
        } else if (totalProfitLoss < 0) {
            emoji = "📉";
        } else {
            emoji = "➖";
        }
        
        String profitText = totalProfitLoss > 0 ? "盈利" : (totalProfitLoss < 0 ? "亏损" : "持平");
        return String.format("%s 股票日报 - 今日%s %.2f 元", emoji, profitText, Math.abs(totalProfitLoss));
    }

    /**
     * 构建消息内容（Markdown 格式）
     */
    private String buildDesp(int stockCount, double totalProfitLoss, String stockDetails) {
        StringBuilder desp = new StringBuilder();
        
        // 汇总信息
        desp.append("## 📊 今日持仓概览\n\n");
        desp.append(String.format("**持仓股票数**: %d 只\n", stockCount));
        desp.append(String.format("**今日盈亏**: %s %.2f 元\n\n", 
                totalProfitLoss >= 0 ? "✅" : "❌", totalProfitLoss));
        
        // 明细信息
        if (stockDetails != null && !stockDetails.trim().isEmpty()) {
            desp.append("## 📝 个股详情\n\n");
            desp.append(stockDetails);
        }
        
        // 时间戳
        desp.append("\n\n---\n");
        desp.append(String.format("*推送时间*: %s*", 
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        return desp.toString();
    }

    /**
     * 构建表单数据
     */
    private String buildFormData(Map<String, Object> params) {
        StringBuilder formData = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (formData.length() > 0) {
                formData.append("&");
            }
            try {
                String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString());
                String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.toString());
                formData.append(key).append("=").append(value);
            } catch (Exception e) {
                log.error("URL 编码失败：{}", e.getMessage());
            }
        }
        
        return formData.toString();
    }

    /**
     * 设置 SendKey（用于测试）
     */
    public void setSendkey(String sendkey) {
        this.sendkey = sendkey;
    }
}
