package com.estudy.point.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("point_account")
public class PointAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal totalEarned;
    private Integer yearYear;
    private BigDecimal yearEarned;
    private BigDecimal yearSpent;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
