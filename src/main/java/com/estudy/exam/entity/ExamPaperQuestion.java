package com.estudy.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private Long questionId;

    private Integer sort;

    private Integer score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
