package com.estudy.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.estudy.course.entity.CourseLesson;

@Data
@TableName("course_chapter")
public class CourseChapter {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long parentId;

    private String title;

    private Integer sort;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：该章节下的课时列表 */
    @TableField(exist = false)
    private List<CourseLesson> lessons;
}
