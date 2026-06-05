package com.estudy.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("exam_paper")
public class ExamPaper {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private Long courseId;

    private String description;

    private Integer totalScore;

    private Integer passScore;

    /** 考试时长(分钟) */
    private Integer duration;

    /** 考试类型 1练习 2正式考试 */
    private Integer examType;

    /** 状态 0草稿 1已发布 2已关闭 */
    private Integer status;

    /** 最大允许次数 */
    private Integer maxAttempts;

    /** 非数据库字段：题目数量 */
    @TableField(exist = false)
    private Integer questionCount;

    /** 非数据库字段：剩余考试次数 */
    @TableField(exist = false)
    private Integer remainAttempts;

    /** 非数据库字段：试卷标题(用于我的成绩) */
    @TableField(exist = false)
    private String paperTitle;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
