package com.venky.validationservice.exception;

import java.util.UUID;

public class ErrorResponse {

    private final String code;
    private final String message;
    private final String validationRequestId;

    public ErrorResponse(String code, String message, String validationRequestId) {
        this.code = code;
        this.message = message;
		this.validationRequestId = validationRequestId;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

	public String getValidationRequestId() {
		return validationRequestId;
	}
}

