package com.venky.validationservice.application.worker;

import java.util.Map;

import com.venky.validationservice.integration.common.ProviderValidationStatus;

public class ProviderResult {

	private final String providerReferenceId; 
	private final ProviderValidationStatus providerStatus; // completed / failed/created (RAW)
	private final Map<String, String> attributes; // name_match_score, account_status
	private final String sanitizedRawPayload; // for storage only
	private final Boolean accountActive;  // derived in parser
    private final String nameMatchScore; // derived in parser
    private final String registeredName;  // derived in parser
    private final String providerAccountStatus;
    private final String bankDetailsJson;
    
	public ProviderResult(String providerReferenceId, ProviderValidationStatus providerStatus,
			Map<String, String> attributes, String sanitizedRawPayload, Boolean accountActive, String nameMatchScore,
			String registeredName, String bankDetailsJson, String providerAccountStatus) {
		this.providerReferenceId = providerReferenceId;
		this.providerStatus = providerStatus;
		this.attributes = attributes;
		this.sanitizedRawPayload = sanitizedRawPayload;
		this.accountActive = accountActive;
		this.nameMatchScore = nameMatchScore;
		this.registeredName = registeredName;
		this.providerAccountStatus = providerAccountStatus;
		this.bankDetailsJson = bankDetailsJson;
	}

	public String getProviderReferenceId() {
		return providerReferenceId;
	}

	public ProviderValidationStatus getProviderStatus() {
		return providerStatus;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getSanitizedRawPayload() {
		return sanitizedRawPayload;
	}

	public Boolean getAccountActive() {
		return accountActive;
	}

	public String getNameMatchScore() {
		return nameMatchScore;
	}

	public String getRegisteredName() {
		return registeredName;
	}

	public String getProviderAccountStatus() {
		return providerAccountStatus;
	}

	public String getBankDetailsJson() {
		return bankDetailsJson;
	}
	
}
