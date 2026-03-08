package com.venky.validationservice.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString(exclude = { "accountNumber", "vpa" }) // PII Safety: Mask or exclude from logs [cite: 89, 233]
public class FundAccountDetails {

	// --- User Information (Shared for both Bank and UPI) ---
	private final String beneficiaryName; // Mandatory for name match score
	private final String email;
	private final String phone;

	// --- Bank Account Details (Optional if VPA is used) ---
	private final String accountNumber;
	private final String ifsc;

	// --- VPA Details (Optional if Bank Account is used) ---
	private final String vpa;

	// Helper to identify the type without using Razorpay strings
	public boolean isBankAccount() {
		return accountNumber != null && ifsc != null;
	}

	public boolean isVpa() {
		return vpa != null;
	}

	@Override
	public String toString() {
		return "FundAccountDetails{" + "beneficiaryName='MASKED', " + "email='MASKED', " + "phone='MASKED', " + "ifsc='"
				+ ifsc + '\'' + '}';
	}
	
	
	
}
