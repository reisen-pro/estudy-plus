package com.estudy.point.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("point_log")
public class PointLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** earn_learn/earn_complete/tip_out/tip_in/redeem/year_reset */
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Long courseId;
    private Long lessonId;
    private Long targetUserId;
    private String description;
    private Integer yearYear;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
