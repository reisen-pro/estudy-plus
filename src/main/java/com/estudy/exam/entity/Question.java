package com.estudy.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long categoryId;

    private String content;

    /** 题型 1单选 2多选 3判断 4填空 */
    private Integer questionType;

    /** 选项JSON */
    private String options;

    private String answer;

    private String analysis;

    /** 难度 1简单 2中等 3困难 */
    private Integer difficulty;

    private Integer score;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
