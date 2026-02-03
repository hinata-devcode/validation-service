package com.venky.validationservice.domain.service;

import com.venky.validationservice.application.ProviderValidationDetails;
import com.venky.validationservice.domain.model.ValidationResult;

public class ValidationExecutionResult {

	private final ValidationResult validationResult;
	private final ProviderValidationDetails providerDetails;

	public ValidationExecutionResult(ValidationResult validationResult, ProviderValidationDetails providerDetails) {
		this.validationResult = validationResult;
		this.providerDetails = providerDetails;
	}

	public ValidationResult getValidationResult() {
		return validationResult;
	}

	public ProviderValidationDetails getProviderDetails() {
		return providerDetails;
	}

}
