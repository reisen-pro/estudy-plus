package com.estudy.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞表
 */
@Data
@TableName("`like`")
public class Like {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标类型 1帖子 2评论 */
    private Integer targetType;

    /** 目标ID */
    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
