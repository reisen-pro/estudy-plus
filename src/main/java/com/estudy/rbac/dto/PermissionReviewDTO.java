package com.estudy.rbac.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionReviewDTO {
    @NotNull(message = "申请ID不能为空")
    private Long applyId;
    /** 1通过 2拒绝 */
    @NotNull(message = "审批结果不能为空")
    private Integer status;
    private String reviewRemark;
}
