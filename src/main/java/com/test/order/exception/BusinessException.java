package com.test.order.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private String reason;

    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(String message, String cause) {
        super(message);
        this.reason = cause;
    }

    public BusinessException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
