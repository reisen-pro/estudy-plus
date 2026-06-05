package com.estudy.dingtalk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DingtalkBindDTO {

    @NotBlank(message = "钉钉用户ID不能为空")
    private String dingUserId;

    private String dingName;

    private String dingAvatar;
}
