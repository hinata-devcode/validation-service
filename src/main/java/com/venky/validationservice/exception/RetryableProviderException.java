package com.venky.validationservice.exception;

public class RetryableProviderException extends ThirdpartyProviderException {
    public RetryableProviderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
