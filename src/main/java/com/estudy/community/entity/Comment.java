package com.estudy.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论表
 */
@Data
@TableName("comment")
public class  Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子ID*/
    private Long postId;

    /** 用户ID*/
    private Long userId;

    /** 父评论ID(0为顶级评论) */
    private Long parentId;

    /** 回复目标用户ID*/
    private Long replyToUserId;

     /** 内容*/
    private String content;

     /** 点赞数*/
    private Integer likeCount;

    /** 状态 0隐藏 1正常 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：评论者名 */
    @TableField(exist = false)
    private String userName;

    /** 非数据库字段：回复目标用户名 */
    @TableField(exist = false)
    private String replyToUserName;

    /** 非数据库字段：子评论 */
    @TableField(exist = false)
    private List<Comment> children;
}
