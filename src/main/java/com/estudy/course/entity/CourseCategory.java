package com.estudy.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("course_category")
public class CourseCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String categoryName;

    private Integer sort;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：子分类 */
    @TableField(exist = false)
    private List<CourseCategory> children;
}
