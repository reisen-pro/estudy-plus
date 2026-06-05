package com.estudy.point.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("point_exchange")
public class PointExchange {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long goodsId;
    private BigDecimal pointsCost;
    /** 0待发放 1已发放 2已取消 */
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
