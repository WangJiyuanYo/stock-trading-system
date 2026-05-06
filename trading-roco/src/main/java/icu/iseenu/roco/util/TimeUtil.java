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
        int currentHour = now.getHour();
        
        // 如果当前时间在 00:00-08:00 之间，属于尚未开市
        if (currentHour < 8) {
            // 计算距离今天8点的剩余时间
            ZonedDateTime todayStart = now.withHour(8).withMinute(0).withSecond(0).withNano(0);
            long remainingMinutes = java.time.Duration.between(now, todayStart).toMinutes();
            long hours = remainingMinutes / 60;
            long minutes = remainingMinutes % 60;
            String countdownStr = hours + "小时" + minutes + "分钟";
            return new RoundInfo("未开放", 4, countdownStr);
        }
        
        // 如果当前时间在 20:00-24:00 之间，今日已收市
        if (currentHour >= 20) {
            // 计算距离明天8点的剩余时间
            ZonedDateTime tomorrowStart = now.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
            long remainingMinutes = java.time.Duration.between(now, tomorrowStart).toMinutes();
            long hours = remainingMinutes / 60;
            long minutes = remainingMinutes % 60;
            String countdownStr = hours + "小时" + minutes + "分钟";
            return new RoundInfo(4, 4, "明日" + countdownStr + "后开市");
        }
        
        // 计算当前轮次：08-12为第1轮，12-16为第2轮，16-20为第3轮，20-24为第4轮
        ZonedDateTime todayStart = now.withHour(8).withMinute(0).withSecond(0).withNano(0);
        long deltaSeconds = java.time.Duration.between(todayStart, now).getSeconds();
        int roundIndex = (int) (deltaSeconds / (4 * 3600)) + 1;
        
        // 确保轮次在1-4范围内
        roundIndex = Math.max(1, Math.min(4, roundIndex));
        
        // 计算本轮结束时间
        ZonedDateTime roundEnd = todayStart.plusHours(roundIndex * 4);
        long remainingSeconds = java.time.Duration.between(now, roundEnd).getSeconds();
        
        // 如果剩余时间为负数，说明已经过了本轮结束时间，进入下一轮或已收市
        if (remainingSeconds < 0) {
            if (roundIndex < 4) {
                roundIndex++;
                roundEnd = todayStart.plusHours(roundIndex * 4);
                remainingSeconds = java.time.Duration.between(now, roundEnd).getSeconds();
            } else {
                // 第4轮已结束
                ZonedDateTime tomorrowStart = now.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
                long remainingMinutes = java.time.Duration.between(now, tomorrowStart).toMinutes();
                long hours = remainingMinutes / 60;
                long minutes = remainingMinutes % 60;
                String countdownStr = hours + "小时" + minutes + "分钟";
                return new RoundInfo(4, 4, "明日" + countdownStr + "后开市");
            }
        }
        
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
