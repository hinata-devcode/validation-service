package com.venky.validationservice.domain.service;

import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ValidationState;

public interface ProviderValidationPort {
	void validate(FundAccountDetails details,ValidationState validationState);
	Provider getProviderName();
}
