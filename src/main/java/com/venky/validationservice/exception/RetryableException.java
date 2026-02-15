package com.venky.validationservice.exception;

import com.venky.validationservice.integration.razorpay.RzpException;

public class RetryableException extends RzpException {

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}

