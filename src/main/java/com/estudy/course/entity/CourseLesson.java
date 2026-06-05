package com.estudy.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_lesson")
public class CourseLesson {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long chapterId;

    private Long courseId;

    private String title;

    /** 课时类型 1视频 2文档 3链接 */
    private Integer lessonType;

    private String mediaUrl;

    /** 视频时长(秒) */
    private Integer duration;

    private Integer sort;

    /** 是否免费试看 0否 1是 */
    private Integer isFree;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
