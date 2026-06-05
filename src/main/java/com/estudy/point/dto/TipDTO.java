package com.estudy.point.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TipDTO {
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    @NotNull(message = "打赏积分不能为空")
    private BigDecimal amount;
}
