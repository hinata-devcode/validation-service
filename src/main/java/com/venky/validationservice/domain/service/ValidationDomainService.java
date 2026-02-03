package com.venky.validationservice.domain.service;

import com.venky.validationservice.domain.model.ConfidenceLevel;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.integration.common.ProviderValidationResult;

public class ValidationDomainService {

	private final ProviderValidationPort providerPort;

	public ValidationDomainService(ProviderValidationPort providerPort) {
		this.providerPort = providerPort;
	}

	   public ValidationExecutionResult validate(FundAccountDetails details) {

	        // Call provider
	        ProviderValidationResult providerResult =
	                providerPort.validate(details);

	        ProviderValidationResponse response =
	                providerResult.getDecisionData();

	        // Apply domain rules
	        ValidationResult validationResult;

	        if (!response.isSuccess()) {
	            validationResult = ValidationResult.failure();
	        } else if (response.getNameMatchScore() >= 90) {
	            validationResult =
	                    ValidationResult.success(ConfidenceLevel.HIGH);
	        } else {
	            validationResult =
	                    ValidationResult.success(ConfidenceLevel.MEDIUM);
	        }

	        // Return BOTH
	        return new ValidationExecutionResult(
	                validationResult,
	                providerResult.getProviderDetails()
	        );
	    }

}
