package com.estudy.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message);  // code = 500
    }

    public BusinessException(int code, String message) {
        super(code, message);  // 透传
    }
}
