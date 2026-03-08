package com.venky.validationservice.integration.razorpay;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccount {
    private final String name;
    private final String ifsc;
    @JsonProperty("account_number")
    private final String accountNumber;
    
	@Override
	public String toString() {
		String maskedAccount = (accountNumber != null && accountNumber.length() > 4)
				? "XXXX" + accountNumber.substring(accountNumber.length() - 4)
				: "MASKED";

		return "BankAccount{" + "name='MASKED', " + "ifsc='" + ifsc + '\'' + ", accountNumber='" + maskedAccount + '\''
				+ '}';
	}
}
