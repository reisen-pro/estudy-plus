package com.estudy.point.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RedeemDTO {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
}
