package com.estudy.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitExamDTO {

    @NotNull(message = "考试记录ID不能为空")
    private Long recordId;

    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String userAnswer;
    }
}
