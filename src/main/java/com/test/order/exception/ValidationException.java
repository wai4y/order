package com.test.order.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException{

    private String reason;

    public ValidationException(String message) {
        super(message);
    }
    public ValidationException(String message, String cause) {
        super(message);
        this.reason = cause;
    }

    public ValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
