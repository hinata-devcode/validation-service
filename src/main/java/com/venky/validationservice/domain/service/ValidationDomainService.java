package com.venky.validationservice.domain.service;

import com.venky.validationservice.domain.model.ConfidenceLevel;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.exception.ValidationExecutionException;
import com.venky.validationservice.integration.common.ValidationExecutionResult;
import com.venky.validationservice.integration.common.ValidationState;
import com.venky.validationservice.integration.razorpay.RzpException;

public class ValidationDomainService {

	private final ProviderValidationPort providerPort;

	public ValidationDomainService(ProviderValidationPort providerPort) {
		this.providerPort = providerPort;
	}

	public ValidationExecutionResult validate(FundAccountDetails details, ValidationState validationState) {
		try {

			// Call provider
			ValidationExecutionResult execution = providerPort.validate(details, null);

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
