package com.estudy.dingtalk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DingtalkLoginDTO {

    @NotBlank(message = "授权码不能为空")
    private String authCode;
}
