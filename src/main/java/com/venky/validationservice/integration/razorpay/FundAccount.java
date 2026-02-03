package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FundAccount {
	@JsonProperty("account_type")
	private String accountType;
	@JsonProperty("bank_account")
	private BankAccount bankAccount;
	@JsonProperty("contact")
	private Contact contact;
}
