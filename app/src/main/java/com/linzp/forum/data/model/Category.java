package com.linzp.forum.data.model;

import java.io.Serializable;

/**
 * 板块。内容基本是静态的，先写死在代码里，后面如果要做后台配置再落库。
 */
public class Category implements Serializable {

    private final int id;
    private final String name;
    private final String description;
    private final int colorRes;

    public Category(int id, String name, String description, int colorRes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.colorRes = colorRes;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getColorRes() {
        return colorRes;
    }

    /**
     * 所有板块的静态列表。
     * id 和 TopicEntity.categoryId 对应，不要随意改顺序。
     */
    public static final Category[] ALL = new Category[]{
            new Category(0, "全部", "所有板块的帖子都在这里", 0xFF4A6CF7),
            new Category(1, "综合讨论", "随便聊聊，什么都可以", 0xFF4A6CF7),
            new Category(2, "技术交流", "代码、工具、踩坑记录", 0xFF2FBF71),
            new Category(3, "生活日常", "吃饭、旅行、养猫养狗", 0xFFF5A623),
            new Category(4, "求助问答", "遇到问题来这里问", 0xFFE5484D),
            new Category(5, "资源分享", "好用的软件和资料", 0xFF9B59B6),
    };

    public static String nameOf(int categoryId) {
        for (Category c : ALL) {
            if (c.getId() == categoryId) {
                return c.getName();
            }
        }
        return "未知板块";
    }

    public static Category findById(int categoryId) {
        for (Category c : ALL) {
            if (c.getId() == categoryId) {
                return c;
            }
        }
        return ALL[0];
    }
}
