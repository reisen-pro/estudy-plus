package com.estudy.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /** 任务类型 1=学习+考试 2=纯考试 3=纯学习 */
    private Integer taskType;

    /** 推送部门ID，NULL=全员 */
    private Long deptId;

    /** 关联课程ID */
    private Long courseId;

    /** 关联试卷ID */
    private Long paperId;

    /** 文档课时要求阅读时长(秒) */
    private Integer docReadDuration;

    private LocalDateTime deadline;

    /** 状态 1进行中 2已结束 */
    private Integer status;

    private Long createBy;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== 非数据库字段 =====

    @TableField(exist = false)
    private String courseTitle;

    @TableField(exist = false)
    private String paperTitle;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private Integer learnStatus;

    @TableField(exist = false)
    private Integer examStatus;

    @TableField(exist = false)
    private Integer taskStatus;
}
