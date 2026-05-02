package icu.iseenu.roco.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP客户端工具类
 */
public class HttpClientUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int TIMEOUT_MS = 30000;
    
    /**
     * 发送GET请求
     */
    public static String sendGet(String url, Map<String, String> headers) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            
            // 设置超时
            request.setConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                    .setConnectionRequestTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(TIMEOUT_MS))
                    .setResponseTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(TIMEOUT_MS))
                    .build());
            
            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.setHeader(entry.getKey(), entry.getValue());
                }
            }
            
            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                if (statusCode >= 200 && statusCode < 300) {
                    return body;
                } else {
                    throw new IOException("HTTP请求失败，状态码: " + statusCode + ", 响应: " + body);
                }
            });
        }
    }
    
    /**
     * 发送POST JSON请求
     */
    public static String sendPostJson(String url, String jsonBody, Map<String, String> headers) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            
            // 设置超时
            request.setConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                    .setConnectionRequestTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(TIMEOUT_MS))
                    .setResponseTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(TIMEOUT_MS))
                    .build());
            
            // 设置请求体
            StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            
            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.setHeader(entry.getKey(), entry.getValue());
                }
            }
            
            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                if (statusCode >= 200 && statusCode < 300) {
                    return body;
                } else {
                    throw new IOException("HTTP请求失败，状态码: " + statusCode + ", 响应: " + body);
                }
            });
        }
    }
    
    /**
     * 上传文件（multipart/form-data）
     */
    public static String uploadFile(String url, File file, Map<String, String> formFields) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            
            // 设置超时
            request.setConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                    .setConnectionRequestTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(TIMEOUT_MS))
                    .setResponseTimeout(org.apache.hc.core5.util.Timeout.ofMilliseconds(TIMEOUT_MS))
                    .build());
            
            // 构建multipart请求体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            
            // 添加表单字段
            if (formFields != null) {
                for (Map.Entry<String, String> entry : formFields.entrySet()) {
                    builder.addTextBody(entry.getKey(), entry.getValue(), ContentType.TEXT_PLAIN);
                }
            }
            
            // 添加文件
            builder.addBinaryBody("image", file, ContentType.DEFAULT_BINARY, file.getName());
            
            request.setEntity(builder.build());
            
            return httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                if (statusCode >= 200 && statusCode < 300) {
                    return body;
                } else {
                    throw new IOException("文件上传失败，状态码: " + statusCode + ", 响应: " + body);
                }
            });
        }
    }
    
    /**
     * 解析JSON字符串为JsonNode
     */
    public static JsonNode parseJson(String jsonString) throws IOException {
        return objectMapper.readTree(jsonString);
    }
}
