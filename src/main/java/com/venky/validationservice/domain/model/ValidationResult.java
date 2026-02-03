package com.venky.validationservice.domain.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidationResult {

	private final ValidationStatus status;
	private final ConfidenceLevel confidenceLevel;

	private ValidationResult(ValidationStatus status, ConfidenceLevel confidenceLevel) {
		this.status = status;
		this.confidenceLevel = confidenceLevel;
	}

	public static ValidationResult success(ConfidenceLevel level) {
		return new ValidationResult(ValidationStatus.SUCCESS, level);
	}

	public static ValidationResult failure() {
		return new ValidationResult(ValidationStatus.FAILED, null);
	}

}
