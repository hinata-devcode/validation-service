package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FundAccount {
	
	@JsonProperty("account_type")
	private final String accountType;
	
	@JsonProperty("bank_account")
	private final BankAccount bankAccount;

	@JsonProperty("vpa")
	private final Vpa vpa;
	
	 @JsonProperty("contact")
	 private Contact contact;
	
}
