package com.estudy.common.exception;

public class PermissionException extends BaseException {
    public PermissionException(String message) {
        super(403,message);
    }
}
