package com.venky.validationservice.domain.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidationResult {

	private final ValidationStatus status;
	private final ConfidenceLevel confidenceLevel;
	 private final ProviderDetails providerDetails; 


}
