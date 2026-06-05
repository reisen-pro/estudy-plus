package com.estudy.dingtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dingtalk_user_bind")
public class DingtalkUserBind {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String dingUserId;

    private String dingName;

    private String dingAvatar;

    private LocalDateTime bindTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
