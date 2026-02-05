package com.venky.validationservice.integration.common;

import java.util.Optional;

public class ValidationExecutionResult {

	private final ValidationState validationState;
	private final Optional<ImmediateValidationOutcome> outcome;

	public ValidationExecutionResult(ValidationState validationState, Optional<ImmediateValidationOutcome> outcome) {
		this.validationState = validationState;
		this.outcome = outcome;
	}

	public ValidationState getValidationState() {
		return validationState;
	}

	public Optional<ImmediateValidationOutcome> getOutcome() {
		return outcome;
	}

}
