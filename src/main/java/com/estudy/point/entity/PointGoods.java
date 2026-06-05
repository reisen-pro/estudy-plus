package com.estudy.point.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("point_goods")
public class PointGoods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal pointsRequired;
    private Integer stock;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
