package com.venky.validationservice.integration.razorpay;

import com.venky.validationservice.domain.model.FundAccountDetails;

public class ContactMapper {

	public Contact createContact(FundAccountDetails accountDetails) {
		return Contact.builder().name(normalize(accountDetails.getBeneficiaryName()))
				.email(normalize(accountDetails.getEmail()))
				// .type(normalize(accountDetails.getContactType()))
				// .referenceId(normalize(accountDetails.getReferenceId()))
				// .notes(accountDetails.getNotes())
				.build();
	}

	private String normalize(String value) {
		return (value == null || value.isBlank()) ? null : value;
	}

}
