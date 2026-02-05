package com.venky.validationservice.integration.razorpay;

import com.venky.validationservice.exception.FailureOrigin;

public class RzpException extends RuntimeException {

    private final FailureOrigin failureOrigin;

    public RzpException(String message, Throwable cause) {
        super(message, cause);
        this.failureOrigin = FailureOrigin.EXTERNAL_PROVIDER;
    }

    public FailureOrigin getFailureOrigin() {
        return failureOrigin;
    }
}

//The serializable class RzpException does not declare a static final serialVersionUID field of type long

