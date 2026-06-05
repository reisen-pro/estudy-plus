package com.estudy.dingtalk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dingtalk")
public class DingtalkProperties {

    /** 钉钉应用 AppKey */
    private String appKey;

    /** 钉钉应用 AppSecret */
    private String appSecret;

    /** 钉钉应用 AgentId */
    private Long agentId;

    /** 回调地址 */
    private String callbackUrl;
}
