package icu.iseenu.roco.util;

import icu.iseenu.roco.model.Product;
import icu.iseenu.roco.model.RoundInfo;
import icu.iseenu.roco.model.TemplateData;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间处理和数据处理工具类
 */
public class TimeUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeUtil.class);
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * 获取北京时间
     */
    public static ZonedDateTime getBeijingTime() {
        return ZonedDateTime.now(BEIJING_ZONE);
    }
    
    /**
     * 格式化时间戳为 HH:mm
     * @param tsMs 毫秒时间戳
     */
    public static String formatTimestamp(long tsMs) {
        try {
            ZonedDateTime dt = ZonedDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(tsMs), 
                BEIJING_ZONE
            );
            return dt.format(TIME_FORMATTER);
        } catch (Exception e) {
            logger.error("时间格式化失败", e);
            return "--:--";
        }
    }
    
    /**
     * 计算当前远行商人的轮次与倒计时
     */
    public static RoundInfo getRoundInfo() {
        ZonedDateTime now = getBeijingTime();
        ZonedDateTime startTime = now.withHour(8).withMinute(0).withSecond(0).withNano(0);
        
        // 如果当前时间早于8点，返回未开放
        if (now.isBefore(startTime)) {
            return new RoundInfo("未开放", 4, "尚未开市");
        }
        
        // 每4小时一轮: 08-12, 12-16, 16-20, 20-00
        long deltaSeconds = java.time.Duration.between(startTime, now).getSeconds();
        int roundIndex = (int) (deltaSeconds / (4 * 3600)) + 1;
        
        if (roundIndex > 4) {
            return new RoundInfo(4, 4, "今日已收市");
        }
        
        // 计算本轮剩余时间
        ZonedDateTime roundEnd = startTime.plusHours(roundIndex * 4);
        long remainingSeconds = java.time.Duration.between(now, roundEnd).getSeconds();
        
        long hours = remainingSeconds / 3600;
        long minutes = (remainingSeconds % 3600) / 60;
        
        String countdownStr;
        if (hours > 0) {
            countdownStr = hours + "小时" + minutes + "分钟";
        } else {
            countdownStr = minutes + "分钟";
        }
        
        return new RoundInfo(roundIndex, 4, countdownStr);
    }
    
    /**
     * 清洗接口数据，精准筛选当前轮次商品
     */
    public static TemplateData processDataForTemplate(JsonNode rawData) {
        if (rawData == null || rawData.isNull()) {
            logger.warn("原始数据为空");
            return new TemplateData();
        }
        
        long nowMs = System.currentTimeMillis();
        RoundInfo roundInfo = getRoundInfo();
        
        // 获取merchantActivities
        JsonNode activities = rawData.get("merchantActivities");
        if (activities == null || !activities.isArray() || activities.size() == 0) {
            logger.warn("没有活动数据");
            return createEmptyTemplateData(roundInfo);
        }
        
        JsonNode activity = activities.get(0);
        
        // 合并get_props和get_pets
        List<Product> activeProducts = new ArrayList<>();
        
        JsonNode getProps = activity.get("get_props");
        if (getProps != null && getProps.isArray()) {
            activeProducts.addAll(extractProducts(getProps, nowMs));
        }
        
        JsonNode getPets = activity.get("get_pets");
        if (getPets != null && getPets.isArray()) {
            activeProducts.addAll(extractProducts(getPets, nowMs));
        }
        
        // 构建模板数据
        TemplateData templateData = new TemplateData();
        templateData.setTitle(activity.has("name") ? activity.get("name").asText() : "远行商人");
        templateData.setSubtitle(activity.has("start_date") ? activity.get("start_date").asText() : 
                                "每日 08:00 / 12:00 / 16:00 / 20:00 刷新");
        templateData.setProductCount(activeProducts.size());
        templateData.setRoundInfo(roundInfo);
        templateData.setProducts(activeProducts);
        
        // 为了适配原版HTML模板
        templateData.setResPath("");
        templateData.setBackground("img/bg.C8CUoi7I.jpg");
        templateData.setTitleIcon(true);
        
        return templateData;
    }
    
    /**
     * 从JSON数组中提取商品列表
     */
    private static List<Product> extractProducts(JsonNode itemsArray, long nowMs) {
        List<Product> products = new ArrayList<>();
        
        for (JsonNode item : itemsArray) {
            JsonNode startTimeNode = item.get("start_time");
            JsonNode endTimeNode = item.get("end_time");
            
            Product product = new Product();
            product.setName(item.has("name") ? item.get("name").asText() : "未知");
            product.setImage(item.has("icon_url") ? item.get("icon_url").asText() : "");
            
            if (startTimeNode != null && endTimeNode != null) {
                long startTime = startTimeNode.asLong();
                long endTime = endTimeNode.asLong();
                
                // 只保留当前时间段内的商品
                if (startTime <= nowMs && nowMs < endTime) {
                    product.setTimeLabel(formatTimestamp(startTime) + " - " + formatTimestamp(endTime));
                    products.add(product);
                }
            } else {
                // 没有时间限制的商品
                product.setTimeLabel("全天供应");
                products.add(product);
            }
        }
        
        return products;
    }
    
    /**
     * 创建空的模板数据
     */
    private static TemplateData createEmptyTemplateData(RoundInfo roundInfo) {
        TemplateData templateData = new TemplateData();
        templateData.setTitle("远行商人");
        templateData.setSubtitle("每日 08:00 / 12:00 / 16:00 / 20:00 刷新");
        templateData.setProductCount(0);
        templateData.setRoundInfo(roundInfo);
        templateData.setProducts(new ArrayList<>());
        templateData.setResPath("");
        templateData.setBackground("img/bg.C8CUoi7I.jpg");
        templateData.setTitleIcon(true);
        return templateData;
    }
}
