package com.venky.validationservice.integration.razorpay;

import org.springframework.stereotype.Component;

import com.venky.validationservice.domain.model.FundAccountDetails;

@Component
public class RzpFundAccountFactory {
	
	private final BankAccountMapper bankMapper;
	private final VpaMapper vpaMapper;
	private final ContactMapper contactMapper;
	private final String bankAccountType="bank_account";
	private  final String vpaType="vpa";

	
	public RzpFundAccountFactory(BankAccountMapper bankMapper, VpaMapper vpaMapper, ContactMapper contactMapper) {
		super();
		this.bankMapper = bankMapper;
		this.vpaMapper = vpaMapper;
		this.contactMapper = contactMapper;
	}

	public FundAccount createFundAccountDetails(FundAccountDetails accountDetails) {
		
		var contact = contactMapper.createContact(accountDetails);
		
		if(accountDetails.isBankAccount()) {
			var bankAccount = bankMapper.createBankAccount(accountDetails);
			return FundAccount.builder()
					.accountType(bankAccountType)
					.bankAccount(bankAccount)
					.contact(contact)
					.build();
		}
		else if(accountDetails.isVpa()){
			var vpa = vpaMapper.createVpa(accountDetails);
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
