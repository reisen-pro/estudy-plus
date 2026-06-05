package com.estudy.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String subtitle;

    private String coverUrl;

    private Long categoryId;

    private Long teacherId;

    private String description;

    /** 课程类型 1点播 2直播 3混合 */
    private Integer courseType;

    private BigDecimal price;

    /** 状态 0草稿 1已发布 2已下架 */
    private Integer status;

    private Integer viewCount;

    /** 非数据库字段：分类名称 */
    @TableField(exist = false)
    private String categoryName;

    /** 非数据库字段：学习人数 */
    @TableField(exist = false)
    private Integer studyCount;

    /** 非数据库字段：讲师名称 */
    @TableField(exist = false)
    private String teacherName;

    /** 非数据库字段：讲师头像 */
    @TableField(exist = false)
    private String teacherAvatar;

    /** 非数据库字段：章节列表(含课时) */
    @TableField(exist = false)
    private List<CourseChapter> chapters;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
