package com.estudy.community.enums;

/**
 * 通知类型枚举
 */
public enum NotificationType {

    /**
     * 帖子被点赞
     */
    LIKE_POST("LIKE_POST", "帖子被点赞"),

    /**
     * 评论被点赞
     */
    LIKE_COMMENT("LIKE_COMMENT", "评论被点赞"),

    /**
     * 收到评论
     */
    COMMENT("COMMENT", "收到评论"),

    /**
     * 收到回复
     */
    REPLY("REPLY", "收到回复"),

    /**
     * 系统通知
     */
    SYSTEM("SYSTEM", "系统通知");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
