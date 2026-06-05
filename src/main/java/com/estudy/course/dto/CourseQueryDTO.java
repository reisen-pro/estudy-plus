package com.estudy.course.dto;

import lombok.Data;

@Data
public class CourseQueryDTO {

    private String keyword;

    private Long categoryId;

    private Integer courseType;

    private Integer status = 1;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
