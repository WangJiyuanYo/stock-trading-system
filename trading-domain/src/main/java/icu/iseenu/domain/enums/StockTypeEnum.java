package icu.iseenu.domain.enums;

import lombok.Getter;

/**
 * 股票类型枚举
 */
@Getter
public enum StockTypeEnum {

    /**
     * A 股 - 中国大陆股市（上海、深圳）
     * 注意：name 故意写作 "A股"（无空格），与 DB 历史数据 / Stock 输入保持一致——
     * 之前写作 "A 股" 导致 fetchAllStockMarketDataWithProfit 的 equals 过滤把所有
     * DB 里实际为 "A股" 的持仓全部排除，定时任务结果为空。
     */
    A_SHARE("A股", "中国大陆 A 股市场"),

    /**
     * 港股 - 香港股市
     */
    HK_SHARE("港股", "香港股票市场"),

    /**
     * 美股 - 美国股市
     */
    US_SHARE("美股", "美国股票市场"),

    /**
     * 英股 - 英国股市
     */
    UK_SHARE("英股", "英国股票市场"),

    /**
     * 贵金属 - 黄金、白银等
     */
    PRECIOUS_METAL("贵金属", "黄金、白银等贵金属");

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 描述
     */
    private final String description;

    StockTypeEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 根据名称获取枚举
     * @param name 类型名称
     * @return 股票类型枚举，未找到返回 A 股
     */
    public static StockTypeEnum fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return A_SHARE;
        }

        String normalized = normalize(name);
        for (StockTypeEnum type : values()) {
            if (normalize(type.getName()).equals(normalized)) {
                return type;
            }
        }

        // 默认返回 A 股
        return A_SHARE;
    }

    /**
     * 判断是否为 A 股
     * @param name 类型名称
     * @return 是否为 A 股
     */
    public static boolean isAShare(String name) {
        if (name == null) {
            return false;
        }
        return normalize(A_SHARE.getName()).equals(normalize(name));
    }

    /**
     * 判断是否为港股
     * @param name 类型名称
     * @return 是否为港股
     */
    public static boolean isHkShare(String name) {
        if (name == null) {
            return false;
        }
        return normalize(HK_SHARE.getName()).equals(normalize(name));
    }

    /**
     * 判断是否为美股
     * @param name 类型名称
     * @return 是否为美股
     */
    public static boolean isUsShare(String name) {
        if (name == null) {
            return false;
        }
        return normalize(US_SHARE.getName()).equals(normalize(name));
    }

    /**
     * 名称归一化：去除所有空白字符。
     * 历史 DB 里同时存在 "A股" 和 "A 股" 两种写法（迁移过程中被空格污染），
     * 这里用归一化吃掉差异，避免相等判定漏掉行。
     */
    private static String normalize(String name) {
        return name.replaceAll("\\s+", "");
    }
}
