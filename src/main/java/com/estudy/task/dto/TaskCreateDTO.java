package com.estudy.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCreateDTO {

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;

    /** 任务类型 1=学习+考试 2=纯考试 3=纯学习 */
    @NotNull(message = "任务类型不能为空")
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
}
