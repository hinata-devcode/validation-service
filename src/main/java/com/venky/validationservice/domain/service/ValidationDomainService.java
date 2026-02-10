package com.venky.validationservice.domain.service;

import com.venky.validationservice.domain.model.ConfidenceLevel;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.exception.ValidationExecutionException;
import com.venky.validationservice.integration.common.ValidationExecutionResult;
import com.venky.validationservice.integration.common.ValidationState;
import com.venky.validationservice.integration.razorpay.RzpException;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

public class ValidationDomainService {

	private final ProviderValidationPort providerPort;
	private final ValidationPersistenceService validationPersistenceService;

	public ValidationDomainService(ProviderValidationPort providerPort, ValidationPersistenceService validationPersistenceService) {
		this.providerPort = providerPort;
		this.validationPersistenceService = validationPersistenceService;
	}

	public ValidationExecutionResult validate(FundAccountDetails details, ValidationState validationState) {
		try {

			// Call provider
			
			validationPersistenceService.createValidationRequest(validationState.getValidationRequestId());
			ValidationExecutionResult execution = providerPort.validate(details, validationState);

			return execution;

		}

		catch (RzpException ex) {
			throw new ValidationExecutionException("Validation could not be initiated", FailureOrigin.EXTERNAL_PROVIDER,
					ex);

		} catch (RuntimeException ex) {
			throw new ValidationExecutionException("Internal validation error", FailureOrigin.INTERNAL_SYSTEM, ex);
		}
	}

}
