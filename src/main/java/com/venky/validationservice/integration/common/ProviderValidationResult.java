package com.venky.validationservice.integration.common;

import com.venky.validationservice.application.ProviderValidationDetails;
import com.venky.validationservice.domain.service.ProviderValidationResponse;

public class ProviderValidationResult {

	private final ProviderValidationResponse decisionData;
	private final ProviderValidationDetails providerDetails;

	public ProviderValidationResult(ProviderValidationResponse decisionData,
			ProviderValidationDetails providerDetails) {
		this.decisionData = decisionData;
		this.providerDetails = providerDetails;
	}

	public ProviderValidationResponse getDecisionData() {
		return decisionData;
	}

	public ProviderValidationDetails getProviderDetails() {
		return providerDetails;
	}

}
