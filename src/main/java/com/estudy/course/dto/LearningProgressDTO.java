package com.estudy.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LearningProgressDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "课时ID不能为空")
    private Long lessonId;

    /** 学习进度百分比 0-100 */
    private Integer progress;

    /** 已学时长(秒) */
    private Integer learnDuration;

    /** 上次播放位置(秒) */
    private Integer lastPosition;
}
