package com.estudy.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知表
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 触发者ID */
    private Long fromUserId;

    /** 通知类型 like/comment/reply/system */
    private String type;

    private String title;

    private String content;

    /** 业务ID */
    private String bizId;

    /** 是否已读 0未读 1已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 非数据库字段：触发者名 */
    @TableField(exist = false)
    private String fromUserName;
}
