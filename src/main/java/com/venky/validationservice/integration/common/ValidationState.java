package com.venky.validationservice.integration.common;

import com.venky.validationservice.domain.model.ConfidenceLevel;

public class ValidationState {

    private final String validationRequestId;
    private Provider provider;
    private String providerReferenceId;
    private ExecutionStatus executionStatus;

    public ValidationState(String validationRequestId) {
        this.validationRequestId = validationRequestId;
        this.setExecutionStatus(ExecutionStatus.INITIATED);
    }

	public String getValidationRequestId() {
		return validationRequestId;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public String getProviderReferenceId() {
		return providerReferenceId;
	}

	public void setProviderReferenceId(String providerReferenceId) {
		this.providerReferenceId = providerReferenceId;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}

    
}

