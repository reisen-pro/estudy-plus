package com.estudy.common.exception;

import lombok.Getter;

/**
 * 自定义异常
 */
@Getter
public class BaseException extends RuntimeException{
    private final int code;

    public BaseException(String message) {
        super(message);
        this.code = 500;
    }

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }
}
