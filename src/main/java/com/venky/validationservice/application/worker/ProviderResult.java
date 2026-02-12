package com.venky.validationservice.application.worker;

import java.util.Map;

public class ProviderResult {

	private final String providerReferenceId; // fav_xxx
	private final String providerStatus; // completed / failed (RAW)
	private final Map<String, String> attributes; // name_match_score, account_status
	private final String sanitizedRawPayload; // for storage only

	public ProviderResult(String providerReferenceId, String providerStatus, Map<String, String> attributes,
			String sanitizedRawPayload) {
		this.providerReferenceId = providerReferenceId;
		this.providerStatus = providerStatus;
		this.attributes = attributes;
		this.sanitizedRawPayload = sanitizedRawPayload;
	}

	public String getProviderReferenceId() {
		return providerReferenceId;
	}

	public String getProviderStatus() {
		return providerStatus;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getSanitizedRawPayload() {
		return sanitizedRawPayload;
	}
}
