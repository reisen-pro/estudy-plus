package com.estudy.dingtalk.controller;

import com.estudy.common.result.Result;
import com.estudy.dingtalk.config.DingtalkProperties;
import com.estudy.dingtalk.dto.DingtalkBindDTO;
import com.estudy.dingtalk.dto.DingtalkLoginDTO;
import com.estudy.dingtalk.dto.NotifyDTO;
import com.estudy.dingtalk.service.DingtalkAuthService;
import com.estudy.dingtalk.service.DingtalkNotifyService;
import com.estudy.user.dto.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dingtalk")
@RequiredArgsConstructor
public class DingtalkController {

    private final DingtalkAuthService authService;
    private final DingtalkNotifyService notifyService;
    private final DingtalkProperties properties;
    private final HttpServletRequest request;

    /**
     * 获取钉钉扫码登录URL
     */
    @GetMapping("/login-url")
    public Result<String> getLoginUrl(@RequestParam(defaultValue = "") String state) {
        String url = "https://login.dingtalk.com/login/qrcode.htm"
                + "?appkey=" + properties.getAppKey()
                + "&redirect_uri=" + properties.getCallbackUrl()
                + "&state=" + state
                + "&response_type=code"
                + "&scope=openid";
        return Result.success(url);
    }

    /**
     * 钉钉扫码登录回调
     */
    @PostMapping("/login")
    public Result<LoginVO> loginByDingtalk(@RequestBody @Valid DingtalkLoginDTO dto) {
        return Result.success(authService.loginByDingtalk(dto.getAuthCode()));
    }

    /**
     * 绑定钉钉账号
     */
    @PostMapping("/bind")
    public Result<Void> bindDingtalk(@RequestBody @Valid DingtalkBindDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        authService.bindDingtalk(userId, dto.getDingUserId(), dto.getDingName(), dto.getDingAvatar());
        return Result.success();
    }

    /**
     * 解绑钉钉账号
     */
    @DeleteMapping("/bind")
    public Result<Void> unbindDingtalk() {
        Long userId = (Long) request.getAttribute("userId");
        authService.unbindDingtalk(userId);
        return Result.success();
    }

    /**
     * 发送通知(管理员接口)
     */
    @PostMapping("/notify")
    public Result<Void> sendNotify(@RequestBody @Valid NotifyDTO dto) {
        switch (dto.getMsgType()) {
            case "training_notice" -> notifyService.sendTrainingNotice(dto.getUserId(), dto.getTitle(), dto.getContent());
            case "exam_notice" -> notifyService.sendExamNotice(dto.getUserId(), dto.getTitle(), dto.getContent());
            default -> notifyService.sendMarkdownNotice(dto.getUserId(), dto.getMsgType(), dto.getTitle(), dto.getContent(), dto.getBizId());
        }
        return Result.success();
    }
}
