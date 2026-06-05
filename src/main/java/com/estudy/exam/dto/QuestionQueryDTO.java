package com.estudy.exam.dto;

import lombok.Data;

@Data
public class QuestionQueryDTO {

    private Long categoryId;

    private Integer questionType;

    private Integer difficulty;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
