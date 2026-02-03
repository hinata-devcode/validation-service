package com.venky.validationservice.integration.razorpay;

import com.venky.validationservice.application.ProviderValidationDetails;

public class RazorpayValidationDetails implements ProviderValidationDetails {

	private final String registeredName;
	private final int nameMatchScore;

	public RazorpayValidationDetails(String registeredName, int nameMatchScore) {
		this.registeredName = registeredName;
		this.nameMatchScore = nameMatchScore;
	}

	@Override
	public String providerName() {
		return "RAZORPAY";
	}

	public String getRegisteredName() {
		return registeredName;
	}

	public int getNameMatchScore() {
		return nameMatchScore;
	}
}
