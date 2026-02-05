package com.venky.validationservice.integration.common;

public class ImmediateValidationOutcome {

    private final boolean valid;
    private final String reason;

    public ImmediateValidationOutcome(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

	public boolean isValid() {
		return valid;
	}

	public String getReason() {
		return reason;
	}

    
}

