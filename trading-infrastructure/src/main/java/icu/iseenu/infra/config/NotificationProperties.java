package icu.iseenu.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

/**
 * 通知配置属性
 * 统一管理 application.yml 中的 notification.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    
    /**
     * 启用的通知渠道（逗号分隔）
     */
    private String enabledChannels = "";
    
    /**
     * Server 酱配置
     */
    private ServerChan serverchan = new ServerChan();
    
    /**
     * NotifyMe 配置
     */
    private NotifyMe notifyme = new NotifyMe();

    /**
     * 飞书配置
     */
    private Feishu feishu = new Feishu();

    public boolean isChannelEnabled(String channel) {
        if (channel == null || channel.isBlank() || enabledChannels == null) {
            return false;
        }
        return Arrays.stream(enabledChannels.split(","))
                .map(String::trim)
                .anyMatch(channel::equalsIgnoreCase);
    }

    @Data
    public static class ServerChan {
        /**
         * Server 酱 SendKey
         */
        private String sckey = "";
    }

    @Data
    public static class NotifyMe {
        /**
         * NotifyMe UUID（旧版单值配置，保留兼容）
         */
        private String uuid = "";

        /**
         * NotifyMe UUID 列表
         */
        private List<String> uuids = new ArrayList<>();

        /**
         * NotifyMe 用户及其订阅场景。
         * 适合在 YAML 中进行可读配置。
         */
        private List<NotifyMeUser> users = new ArrayList<>();

        /**
         * 环境变量用户配置，格式：
         * 用户名|UUID|场景1,场景2;用户名|UUID|场景1
         */
        private String usersConfig = "";

        /**
         * NotifyMe 服务地址
         */
        private String baseUrl = "https://notifyme-server.wzn556.top/?";

        /**
         * 合并新旧配置，过滤空值并保持配置顺序去重。
         * 同时兼容环境变量中使用逗号分隔 UUID 的写法。
         */
        public List<String> getResolvedUuids() {
            Set<String> resolvedUuids = new LinkedHashSet<>();
            addUuids(resolvedUuids, uuids);
            addUuid(resolvedUuids, uuid);
            return new ArrayList<>(resolvedUuids);
        }

        /**
         * 根据通知场景查找已订阅用户。
         * 用户订阅 "*" 时接收所有场景；没有配置 users 时回退旧版配置。
         */
        public List<String> getResolvedUuids(String scene) {
            String normalizedScene = scene == null ? "" : scene.trim().toLowerCase();
            List<NotifyMeUser> resolvedUsers = getResolvedUsers();
            if (resolvedUsers.isEmpty() && (usersConfig == null || usersConfig.isBlank())) {
                return getResolvedUuids();
            }

            Set<String> resolvedUuids = new LinkedHashSet<>();
            for (NotifyMeUser user : resolvedUsers) {
                if (user != null && user.subscribesTo(normalizedScene)) {
                    addUuid(resolvedUuids, user.getUuid());
                }
            }
            return new ArrayList<>(resolvedUuids);
        }

        private List<NotifyMeUser> getResolvedUsers() {
            List<NotifyMeUser> resolvedUsers = new ArrayList<>();
            if (users != null) {
                resolvedUsers.addAll(users);
            }
            if (usersConfig == null || usersConfig.isBlank()) {
                return resolvedUsers;
            }

            for (String userConfig : usersConfig.split(";")) {
                String[] parts = userConfig.trim().split("\\|", 3);
                if (parts.length < 3) {
                    continue;
                }
                NotifyMeUser user = new NotifyMeUser();
                user.setName(parts[0].trim());
                user.setUuid(parts[1].trim());
                List<String> subscriptions = new ArrayList<>();
                for (String subscription : parts[2].split(",")) {
                    String normalizedSubscription = subscription.trim();
                    if (!normalizedSubscription.isEmpty()) {
                        subscriptions.add(normalizedSubscription);
                    }
                }
                user.setSubscribe(subscriptions);
                resolvedUsers.add(user);
            }
            return resolvedUsers;
        }

        private void addUuids(Set<String> resolvedUuids, List<String> configuredUuids) {
            if (configuredUuids == null) {
                return;
            }
            configuredUuids.forEach(value -> addUuid(resolvedUuids, value));
        }

        private void addUuid(Set<String> resolvedUuids, String configuredUuid) {
            if (configuredUuid == null) {
                return;
            }
            for (String value : configuredUuid.split(",")) {
                String trimmedValue = value.trim();
                if (!trimmedValue.isEmpty()) {
                    resolvedUuids.add(trimmedValue);
                }
            }
        }

        @Data
        public static class NotifyMeUser {
            /**
             * 用户名称，仅用于配置识别和日志排查。
             */
            private String name = "";

            /**
             * 用户的 NotifyMe UUID。
             */
            private String uuid = "";

            /**
             * 订阅的业务场景，例如 stock、roco；"*" 表示全部场景。
             */
            private List<String> subscribe = new ArrayList<>();

            public boolean subscribesTo(String scene) {
                if (uuid == null || uuid.trim().isEmpty() || subscribe == null) {
                    return false;
                }
                String normalizedScene = scene == null ? "" : scene.trim().toLowerCase();
                return subscribe.stream()
                        .filter(value -> value != null)
                        .map(value -> value.trim().toLowerCase())
                        .anyMatch(value -> "*".equals(value) || value.equals(normalizedScene));
            }
        }
    }

    @Data
    public static class Feishu {
        /**
         * 飞书机器人 Webhook URL（用于广播通知到群聊）
         * 例如：https://open.feishu.cn/open-apis/bot/v2/hook/xxx
         */
        private String webhookUrl = "";
    }
}
