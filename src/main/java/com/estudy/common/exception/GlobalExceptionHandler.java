package com.estudy.common.exception;

import com.estudy.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * RestControllerAdvice 是 Spring Boot 的全局异常处理注解
 * 它是 @ControllerAdvice 和 @ResponseBody 的组合。
 *
 * ControllerAdvice 是 Spring 的控制器增强注解，用于定义全局性的 Controller 辅助组件。
 * 核心含义
 * Controller：针对控制器层
 * Advice：增强/通知（AOP概念，表示在某个位置添加额外逻辑）
 * 合起来就是：对所有 Controller 进行增强处理
 * 主要用途
 * 全局异常处理（最常用）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 抛出自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     * MethodArgumentNotValidException = "我看懂了，但不符合规则"
     * 比如说,用户名为空,密码为空,密码长度不够等
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.error(400, msg);
    }

    /**
     * 参数绑定异常
     * BindException = "我看不懂你传的值是什么类型"
     * 比如说,我要的字段是int,你传给我string,就会触发绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        return Result.error(400, msg);
    }

    /**
     * 系统异常
     * 捕获所有未处理的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后重试");
    }

    /**
     * 权限异常
     * 捕获所有未处理的异常
     */
    @ExceptionHandler(PermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handlePermissionException(PermissionException e) {
        log.warn("权限异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
}
