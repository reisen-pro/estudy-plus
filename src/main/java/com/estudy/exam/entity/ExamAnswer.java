package com.estudy.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_answer")
public class ExamAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private Long questionId;

    private String userAnswer;

    /** 是否正确 0否 1是 */
    private Integer isCorrect;

    private Integer score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
