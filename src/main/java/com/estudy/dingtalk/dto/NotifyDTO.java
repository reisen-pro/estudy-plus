package com.estudy.dingtalk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotifyDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "消息类型不能为空")
    private String msgType;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String bizId;
}
