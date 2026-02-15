package com.venky.validationservice.exception;

public class RetryableProviderException extends RuntimeException {
    public RetryableProviderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
