package com.estudy.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionCreateDTO {

    private Long courseId;

    private Long categoryId;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    /** 题型 1单选 2多选 3判断 4填空 */
    @NotNull(message = "题型不能为空")
    private Integer questionType;

    /** 选项JSON */
    private String options;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String analysis;

    /** 难度 1简单 2中等 3困难 */
    private Integer difficulty;

    private Integer score;
}
