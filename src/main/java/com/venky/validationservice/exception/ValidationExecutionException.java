package com.venky.validationservice.exception;

import java.util.UUID;

public class ValidationExecutionException extends RuntimeException {

    private final FailureOrigin failureOrigin;
    private final UUID validationRequestId;

	public ValidationExecutionException(String message, FailureOrigin failureOrigin, Throwable cause,UUID validationRequestId) {
		super(message, cause);
		this.failureOrigin = failureOrigin;
		this.validationRequestId = validationRequestId;
	}

	public ValidationExecutionException(String message, FailureOrigin failureOrigin,UUID validationRequestId) {
		super(message);
		this.failureOrigin = failureOrigin;
		this.validationRequestId = validationRequestId;
	}

	public FailureOrigin getFailureOrigin() {
        return failureOrigin;
    }

	public UUID getValidationRequestId() {
		return validationRequestId;
	}
}
