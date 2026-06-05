package com.estudy.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PaperCreateDTO {

    @NotBlank(message = "试卷标题不能为空")
    private String title;

    private Long courseId;

    private String description;

    @NotNull(message = "总分不能为空")
    private Integer totalScore;

    @NotNull(message = "及格分不能为空")
    private Integer passScore;

    private Integer duration;

    /** 考试类型 1练习 2正式考试 */
    private Integer examType;

    private Integer maxAttempts;

    /** 题目列表：题目ID + 分值 */
    private List<PaperQuestionItem> questions;

    @Data
    public static class PaperQuestionItem {
        private Long questionId;
        private Integer score;
    }
}
