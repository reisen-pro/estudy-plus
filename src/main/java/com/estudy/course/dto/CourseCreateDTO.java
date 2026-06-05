package com.estudy.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseCreateDTO {

    @NotBlank(message = "课程标题不能为空")
    private String title;

    private String subtitle;

    private String coverUrl;

    private Long categoryId;

    private Long teacherId;

    private String description;

    /** 课程类型 1点播 2直播 3混合 */
    @NotNull(message = "课程类型不能为空")
    private Integer courseType;

    private BigDecimal price;
}
