package com.venky.validationservice.integration.common;

import java.util.UUID;

public class ValidationState {

    private final UUID validationRequestId;
    private Provider provider;
    private String providerReferenceId;
    private ExecutionStatus executionStatus;
    private final String incomingHash;
    private final String idempotencyKey;

    public ValidationState(UUID requestId, String incomingHash, String idempotencyKey,ExecutionStatus executionStatus) {
        this.validationRequestId = requestId;
		this.incomingHash = incomingHash;
		this.idempotencyKey = idempotencyKey;
        this.executionStatus=executionStatus;
    }

	public UUID getValidationRequestId() {
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

	public String getIncomingHash() {
		return incomingHash;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void markProcessing(Provider provider, String favId) {
		this.provider = provider;
		this.providerReferenceId = favId;
		this.executionStatus = ExecutionStatus.PROCESSING;
	}

    
}

