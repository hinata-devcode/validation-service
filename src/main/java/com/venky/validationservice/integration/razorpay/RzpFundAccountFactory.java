package com.venky.validationservice.integration.razorpay;

import org.springframework.stereotype.Component;

import com.venky.validationservice.domain.model.FundAccountDetails;

@Component
public class RzpFundAccountFactory {
	
	private final String bankAccountType="bank_account";
	private  final String vpaType="vpa";


	public FundAccount createFundAccountDetails(FundAccountDetails accountDetails) {
		
		var contact = ContactMapper.createContact(accountDetails);
		
		if(accountDetails.isBankAccount()) {
			var bankAccount = BankAccountMapper.createBankAccount(accountDetails);
			return FundAccount.builder()
					.accountType(bankAccountType)
					.bankAccount(bankAccount)
					.contact(contact)
					.build();
		}
		else if(accountDetails.isVpa()){
			var vpa = VpaMapper.createVpa(accountDetails);
			return FundAccount.builder()
					.accountType(vpaType)
					.vpa(vpa)
					.contact(contact)
					.build();
		}
		else
			 throw new IllegalStateException("Neither bank nor VPA present");
		
	}

}
