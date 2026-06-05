package com.estudy.community.dto;

import lombok.Data;

/**
 * 帖子查询参数
 */
@Data
public class PostQueryDTO {

    /** 关键词 */
    private String keyword;

    /** 分类 */
    private String category;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页数量 */
    private Integer pageSize = 10;
}
