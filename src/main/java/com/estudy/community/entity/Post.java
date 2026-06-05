package com.estudy.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子管理表
 */
@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    /** 分类 讨论/分享/提问/公告 */
    private String category;

    /** 浏览数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 是否置顶 0否 1是 */
    private Integer isTop;

    /** 状态 0隐藏 1正常 */
    private Integer status;

    /**
     * TableLogic 是 MyBatis-Plus 的逻辑删除注解。
     */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：作者名 */
    @TableField(exist = false)
    private String authorName;

    /** 非数据库字段：当前用户是否已点赞 */
    @TableField(exist = false)
    private Boolean liked;
}
