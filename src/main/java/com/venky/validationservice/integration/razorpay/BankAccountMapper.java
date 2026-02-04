package com.venky.validationservice.integration.razorpay;

import com.venky.validationservice.domain.model.FundAccountDetails;

public class BankAccountMapper {
	
	public BankAccount createBankAccount(FundAccountDetails accountDetails) {
		return BankAccount.builder()
				          .name(accountDetails.getBeneficiaryName())
				          .ifsc(accountDetails.getIfsc())
				          .accountNumber(accountDetails.getAccountNumber())
				          .build();
	}

}
