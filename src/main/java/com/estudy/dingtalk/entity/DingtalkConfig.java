package com.estudy.dingtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dingtalk_config")
public class DingtalkConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configKey;

    private String configValue;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
