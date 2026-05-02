package icu.iseenu.domain.enums;

import lombok.Getter;

/**
 * 股票类型枚举
 */
@Getter
public enum StockTypeEnum {

    /**
     * A 股 - 中国大陆股市（上海、深圳）
     */
    A_SHARE("A 股", "中国大陆 A 股市场"),

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
        
        for (StockTypeEnum type : values()) {
            if (type.getName().equals(name)) {
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
        return A_SHARE.getName().equals(name);
    }

    /**
     * 判断是否为港股
     * @param name 类型名称
     * @return 是否为港股
     */
    public static boolean isHkShare(String name) {
        return HK_SHARE.getName().equals(name);
    }

    /**
     * 判断是否为美股
     * @param name 类型名称
     * @return 是否为美股
     */
    public static boolean isUsShare(String name) {
        return US_SHARE.getName().equals(name);
    }
}
