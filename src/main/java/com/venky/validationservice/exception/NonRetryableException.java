package com.venky.validationservice.exception;

import com.venky.validationservice.integration.razorpay.RzpException;

public class NonRetryableException extends RzpException {

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
