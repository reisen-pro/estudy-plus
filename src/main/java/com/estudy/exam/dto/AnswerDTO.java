package com.estudy.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerDTO {

    @NotNull(message = "考试记录ID不能为空")
    private Long recordId;

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    private String userAnswer;
}
