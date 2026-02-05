package com.venky.validationservice.domain.service;

import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.integration.common.ValidationExecutionResult;
import com.venky.validationservice.integration.common.ValidationState;

public interface ProviderValidationPort {
	ValidationExecutionResult validate(FundAccountDetails details,ValidationState validationState);
}
