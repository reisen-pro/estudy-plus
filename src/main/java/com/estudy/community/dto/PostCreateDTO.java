package com.estudy.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建帖子
 */
@Data
public class PostCreateDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /** 分类 讨论/分享/提问/公告 */
    private String category;
}
