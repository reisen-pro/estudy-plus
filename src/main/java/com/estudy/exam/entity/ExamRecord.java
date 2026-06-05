package com.estudy.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long paperId;

    private Integer score;

    private Integer totalScore;

    /** 是否及格 0否 1是 */
    private Integer passed;

    /** 状态 0进行中 1已交卷 2已批改 */
    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    /** 用时(秒) */
    private Integer duration;

    /** 非数据库字段：试卷标题 */
    @TableField(exist = false)
    private String paperTitle;

    /** 非数据库字段：格式化用时 */
    @TableField(exist = false)
    private String timeUsed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
