package com.estudy.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_progress")
public class LearningProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    private Long lessonId;

    /** 学习进度百分比 0-100 */
    private Integer progress;

    /** 已学时长(秒) */
    private Integer learnDuration;

    /** 上次播放位置(秒) */
    private Integer lastPosition;

    /** 是否完成 0否 1是 */
    private Integer completed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
