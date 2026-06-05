package com.estudy.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_task")
public class UserTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long taskId;

    /** 学习状态 0=未开始 1=进行中 2=已完成 */
    private Integer learnStatus;

    /** 考试状态 0=未开始 1=进行中 2=已通过 3=未通过 */
    private Integer examStatus;

    /** 整体状态 0=未开始 1=进行中 2=已完成 3=已过期 */
    private Integer taskStatus;

    private LocalDateTime learnCompleteTime;

    private LocalDateTime examCompleteTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
