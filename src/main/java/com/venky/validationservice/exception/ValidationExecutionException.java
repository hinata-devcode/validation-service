package com.venky.validationservice.exception;

public class ValidationExecutionException extends RuntimeException {

    private final FailureOrigin failureOrigin;

    public ValidationExecutionException(
            String message,
            FailureOrigin failureOrigin,
            Throwable cause) {
        super(message, cause);
        this.failureOrigin = failureOrigin;
    }

    public FailureOrigin getFailureOrigin() {
        return failureOrigin;
    }
}
