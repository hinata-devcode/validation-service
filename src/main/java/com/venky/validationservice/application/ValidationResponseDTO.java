package com.venky.validationservice.application;

import com.venky.validationservice.domain.model.ConfidenceLevel;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.domain.model.ValidationStatus;

public class ValidationResponseDTO {

	private final ValidationStatus status;
	private final ConfidenceLevel confidenceLevel;
	private final ProviderValidationDetails providerDetails;

	ValidationResponseDTO(ValidationStatus status, ConfidenceLevel confidenceLevel,
			ProviderValidationDetails providerDetails) {

		this.status = status;
		this.confidenceLevel = confidenceLevel;
		this.providerDetails = providerDetails;
	}

	public static ValidationResponseDTO from(ValidationResult result, ProviderValidationDetails providerDetails) {
		return new ValidationResponseDTO(result.getStatus(), result.getConfidenceLevel(), providerDetails);
	}

	public ValidationStatus getStatus() {
		return status;
	}

	public ConfidenceLevel getConfidenceLevel() {
		return confidenceLevel;
	}

	public ProviderValidationDetails getProviderDetails() {
		return providerDetails;
	}
}
