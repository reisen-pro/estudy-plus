package com.estudy.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建评论
 */
@Data
public class CommentCreateDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    /** 父评论ID(0或不传为顶级评论) */
    private Long parentId;

    /** 回复目标用户ID */
    private Long replyToUserId;
}
