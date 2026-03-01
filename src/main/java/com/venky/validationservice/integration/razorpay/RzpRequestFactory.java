package com.venky.validationservice.integration.razorpay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class RzpRequestFactory {

    private final RazorpayProperties properties;

    public RzpRequestFactory(RazorpayProperties properties) {
        this.properties = properties;
    }

	public RazorpayExternalRequest build(FundAccount fundAccount, UUID validationRequestId) {

		Map<String, String> notes = new HashMap<>();
		notes.put("validation_request_id", validationRequestId.toString());

		String validationType = (fundAccount.getAccountType().equalsIgnoreCase("vpa")) ? "pennydrop"
				: properties.getValidationType();

		return RazorpayExternalRequest.builder().sourceAccountNumber(properties.getSourceAccountNumber())
				.validationType(validationType).fundAccount(fundAccount).referenceId(validationRequestId.toString())
				.notes(notes).build();
	}
}

