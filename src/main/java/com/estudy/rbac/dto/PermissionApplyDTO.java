package com.estudy.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionApplyDTO {
    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;
    private String reason;
}
