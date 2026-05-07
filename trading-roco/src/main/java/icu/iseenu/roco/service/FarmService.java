package icu.iseenu.roco.service;

import com.fasterxml.jackson.databind.JsonNode;
import icu.iseenu.roco.config.AppConfig;
import icu.iseenu.roco.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FarmService {

    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AppConfig config;

    public FarmService(AppConfig config) {
        this.config = config;
    }

    public String getFarmData(String uid) {
        log.info("查询家园信息, uid={}", uid);

        if (!config.hasRocomApiKey()) {
            return "洛克王国API密钥未配置，请在 application.yml 中设置 roco.rocom-api-key";
        }

        try {
            JsonNode homeInfo = fetchHomeInfoJson(uid);
            if (homeInfo == null || homeInfo.isNull()) {
                return "家园信息为空";
            }
            return formatHomeInfo(homeInfo, uid);
        } catch (Exception e) {
            log.error("查询家园信息失败", e);
            return "查询家园信息失败: " + e.getMessage();
        }
    }

    /**
     * 获取即将成熟的作物列表
     * @param uid 玩家UID
     * @param thresholdMinutes 阈值（分钟），距离成熟时间在此范围内的作物视为即将成熟
     * @return 即将成熟的作物文本描述列表，为空表示没有即将成熟的作物
     */
    public List<String> getNearlyRipePlants(String uid, int thresholdMinutes) {
        log.info("检查家园作物成熟状态, uid={}, threshold={}min", uid, thresholdMinutes);

        if (!config.hasRocomApiKey()) {
            log.warn("API密钥未配置，跳过家园监控");
            return List.of();
        }

        try {
            JsonNode homeInfo = fetchHomeInfoJson(uid);
            if (homeInfo == null || homeInfo.isNull()) {
                return List.of();
            }

            return findNearlyRipePlants(homeInfo, uid, thresholdMinutes);
        } catch (Exception e) {
            log.error("检查作物成熟状态失败, uid={}", uid, e);
            return List.of();
        }
    }

    private JsonNode fetchHomeInfoJson(String uid) throws Exception {
        String url = AppConfig.HOME_INFO_API_URL + "?uid=" + uid;
        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-Key", config.getRocomApiKey());

        String response = HttpClientUtil.sendGet(url, headers);
        JsonNode jsonResponse = HttpClientUtil.parseJson(response);

        int code = jsonResponse.has("code") ? jsonResponse.get("code").asInt() : -1;
        if (code != 0) {
            String message = jsonResponse.has("message")
                    ? jsonResponse.get("message").asText() : "未知错误";
            throw new RuntimeException("API返回错误: " + message);
        }

        JsonNode data = jsonResponse.has("data") ? jsonResponse.get("data") : null;
        if (data == null || data.isNull()) {
            return null;
        }

        return data.has("home_info") ? data.get("home_info") : null;
    }

    private String formatHomeInfo(JsonNode homeInfo, String uid) {
        StringBuilder sb = new StringBuilder();

        JsonNode friendBrief = homeInfo.has("friend_home_brief_info")
                ? homeInfo.get("friend_home_brief_info") : null;

        String homeName = "未知";
        int homeLevel = 0;
        if (friendBrief != null && !friendBrief.isNull()) {
            homeName = friendBrief.has("home_name") ? friendBrief.get("home_name").asText() : "未知";
            homeLevel = friendBrief.has("home_level") ? friendBrief.get("home_level").asInt() : 0;
        }

        sb.append("🏡 **家园信息**\n");
        sb.append("UID: ").append(uid).append("\n");
        sb.append("家园名称: ").append(homeName).append("\n");
        sb.append("家园等级: ").append(homeLevel).append("级\n\n");

        JsonNode landList = getLandList(homeInfo);
        if (landList == null) {
            sb.append("暂无种植信息");
            return sb.toString();
        }

        long nowSec = System.currentTimeMillis() / 1000;

        sb.append("**种植信息:**\n");
        for (JsonNode land : landList) {
            int landIndex = land.has("land_index") ? land.get("land_index").asInt() : 0;
            JsonNode plantList = land.has("home_plant_list") ? land.get("home_plant_list") : null;

            if (plantList == null || !plantList.isArray()) {
                continue;
            }

            for (JsonNode plant : plantList) {
                int seedId = plant.has("plant_seed_id") ? plant.get("plant_seed_id").asInt() : 0;
                int plantState = plant.has("plant_state") ? plant.get("plant_state").asInt() : 0;
                long ripTime = plant.has("plant_rip_time") ? plant.get("plant_rip_time").asLong() : 0;
                int slotIndex = plant.has("slot_index") ? plant.get("slot_index").asInt() : 0;

                String ripTimeStr = ripTime > 0
                        ? Instant.ofEpochSecond(ripTime).atZone(BEIJING_ZONE).format(DTF)
                        : "未知";

                String status;
                if (plantState == 1 && ripTime > nowSec) {
                    long remainingSec = ripTime - nowSec;
                    long hours = remainingSec / 3600;
                    long minutes = (remainingSec % 3600) / 60;
                    if (hours > 0) {
                        status = "生长中 (剩余" + hours + "小时" + minutes + "分钟)";
                    } else {
                        status = "生长中 (剩余" + minutes + "分钟)";
                    }
                } else if (plantState == 1 && ripTime <= nowSec) {
                    status = "已成熟，可收获";
                } else {
                    status = "状态未知(plant_state=" + plantState + ")";
                }

                sb.append("- 土地").append(landIndex)
                        .append(" 槽位").append(slotIndex)
                        .append(": 种子").append(seedId)
                        .append(", ").append(status)
                        .append(", 预计成熟: ").append(ripTimeStr)
                        .append("\n");
            }
        }

        return sb.toString();
    }

    private List<String> findNearlyRipePlants(JsonNode homeInfo, String uid, int thresholdMinutes) {
        JsonNode landList = getLandList(homeInfo);
        if (landList == null) {
            return List.of();
        }

        long nowSec = System.currentTimeMillis() / 1000;
        long thresholdSec = thresholdMinutes * 60L;
        List<String> result = new ArrayList<>();

        for (JsonNode land : landList) {
            int landIndex = land.has("land_index") ? land.get("land_index").asInt() : 0;
            JsonNode plantList = land.has("home_plant_list") ? land.get("home_plant_list") : null;

            if (plantList == null || !plantList.isArray()) {
                continue;
            }

            for (JsonNode plant : plantList) {
                int plantState = plant.has("plant_state") ? plant.get("plant_state").asInt() : 0;
                long ripTime = plant.has("plant_rip_time") ? plant.get("plant_rip_time").asLong() : 0;

                if (plantState != 1 || ripTime <= 0) {
                    continue;
                }

                long remainingSec = ripTime - nowSec;
                if (remainingSec <= 0) {
                    // 已成熟
                    String ripTimeStr = Instant.ofEpochSecond(ripTime).atZone(BEIJING_ZONE).format(DTF);
                    int seedId = plant.has("plant_seed_id") ? plant.get("plant_seed_id").asInt() : 0;
                    int slotIndex = plant.has("slot_index") ? plant.get("slot_index").asInt() : 0;
                    result.add(String.format("土地%d槽位%d: 种子%d已成熟，成熟时间%s",
                            landIndex, slotIndex, seedId, ripTimeStr));
                } else if (remainingSec <= thresholdSec) {
                    // 即将成熟
                    String ripTimeStr = Instant.ofEpochSecond(ripTime).atZone(BEIJING_ZONE).format(DTF);
                    int seedId = plant.has("plant_seed_id") ? plant.get("plant_seed_id").asInt() : 0;
                    int slotIndex = plant.has("slot_index") ? plant.get("slot_index").asInt() : 0;
                    long minutes = remainingSec / 60;
                    result.add(String.format("土地%d槽位%d: 种子%d将在%d分钟后成熟，预计%s",
                            landIndex, slotIndex, seedId, minutes, ripTimeStr));
                }
            }
        }

        return result;
    }

    private JsonNode getLandList(JsonNode homeInfo) {
        JsonNode friendCellBrief = homeInfo.has("friend_cell_home_brief_info")
                ? homeInfo.get("friend_cell_home_brief_info") : null;

        if (friendCellBrief == null || friendCellBrief.isNull()) {
            return null;
        }

        JsonNode homePlantInfo = friendCellBrief.has("home_plant_info")
                ? friendCellBrief.get("home_plant_info") : null;

        if (homePlantInfo == null || homePlantInfo.isNull()) {
            return null;
        }

        JsonNode landList = homePlantInfo.has("home_plant_land_list")
                ? homePlantInfo.get("home_plant_land_list") : null;

        if (landList == null || !landList.isArray() || landList.size() == 0) {
            return null;
        }

        return landList;
    }
}
