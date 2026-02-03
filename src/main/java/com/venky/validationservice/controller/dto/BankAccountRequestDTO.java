package com.venky.validationservice.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountRequestDTO {

	@NotBlank(message = "Account number cannot be empty")
	@Pattern(regexp = "^\\d{9,18}$", message = "Account number must be 9-18 digits")
	private String accountNumber;

	@NotBlank(message = "IFSC cannot be empty")
	@Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC format")
	private String ifsc;

	// We mask the data so logs don't leak it

	@Override
	public String toString() {
		return "BankAccountRequestDTO{" + "accountNumber='" + maskAccountNumber(accountNumber) + '\'' + ", ifsc='"
				+ ifsc + '\'' + // IFSC is generally not strictly PII, but safe to log
				'}';
	}

	// Helper method to show only last 4 digits
	private String maskAccountNumber(String account) {
		if (account == null || account.length() < 4) {
			return "REDACTED_INVALID";
		}
		return "XXXX" + account.substring(account.length() - 4);
	}

}
