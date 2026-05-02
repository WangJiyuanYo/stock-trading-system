package icu.iseenu.roco.service;

import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.util.HttpClientUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片上传服务 - ImgBB图床
 */
public class ImageUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageUploadService.class);
    
    private final AppConfig config;
    
    public ImageUploadService(AppConfig config) {
        this.config = config;
    }
    
    /**
     * 上传图片到ImgBB图床
     * @param imagePath 图片文件路径
     * @return 图片URL，失败返回null
     */
    public String uploadToImgbb(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            logger.warn("图片路径为空，跳过上传");
            return null;
        }
        
        if (!config.hasImgbbKey()) {
            logger.warn("未配置IMGBB_KEY，跳过图片上传");
            return null;
        }
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            logger.error("图片文件不存在: {}", imagePath);
            return null;
        }
        
        try {
            // 构建表单数据
            Map<String, String> formFields = new HashMap<>();
            formFields.put("key", config.getImgbbKey());
            
            // 上传文件
            String response = HttpClientUtil.uploadFile(
                AppConfig.IMGBB_UPLOAD_URL, 
                imageFile, 
                formFields
            );
            
            // 解析响应
            JsonNode jsonResponse = HttpClientUtil.parseJson(response);
            
            if (jsonResponse.has("status") && jsonResponse.get("status").asInt() == 200) {
                String imageUrl = jsonResponse.get("data").get("url").asText();
                logger.info("✅ 图床上传成功: {}", imageUrl);
                return imageUrl;
            } else {
                String errorMsg = jsonResponse.has("error") ? 
                    jsonResponse.get("error").get("message").asText() : "未知错误";
                logger.error("❌ 图床上传失败: {}", errorMsg);
                return null;
            }
            
        } catch (IOException e) {
            logger.error("❌ 图床请求异常", e);
            return null;
        }
    }
}
