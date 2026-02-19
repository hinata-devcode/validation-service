package com.venky.validationservice.integration.razorpay;

import org.springframework.stereotype.Component;

@Component
public class RzpRequestFactory {

    private final RazorpayProperties properties;

    public RzpRequestFactory(RazorpayProperties properties) {
        this.properties = properties;
    }

	public RazorpayExternalRequest build(FundAccount fundAccount) {

		String validationType = (fundAccount.getAccountType().equalsIgnoreCase("vpa")) ? "pennydrop"
				: properties.getValidationType();

		return RazorpayExternalRequest.builder().sourceAccountNumber(properties.getSourceAccountNumber())
				.validationType(validationType).fundAccount(fundAccount).build();
	}
}

