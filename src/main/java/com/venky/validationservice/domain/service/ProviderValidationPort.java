package com.venky.validationservice.domain.service;

import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.integration.common.ProviderValidationResult;

public interface ProviderValidationPort {
	ProviderValidationResult validate(FundAccountDetails details);
}
