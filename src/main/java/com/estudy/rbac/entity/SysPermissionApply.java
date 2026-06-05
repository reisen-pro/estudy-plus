package com.estudy.rbac.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_permission_apply")
public class SysPermissionApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String permissionCode;
    private String reason;
    /** 0待审批 1已通过 2已拒绝 */
    private Integer status;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewTime;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String realName;
    @TableField(exist = false)
    private String permissionName;
    @TableField(exist = false)
    private String reviewerName;
}
