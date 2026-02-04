package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FundAccount {
	
	private final String accountType;
	
	private final BankAccount bankAccount;
	
	private final Contact contact;
	
	private final Vpa vpa;
	
}
