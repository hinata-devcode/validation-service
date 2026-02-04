package com.venky.validationservice.integration.razorpay;

import com.venky.validationservice.domain.model.FundAccountDetails;

public class VpaMapper {
	
	public Vpa createVpa(FundAccountDetails accountDetails) {
		return Vpa.builder()
				          .upiId(accountDetails.getVpa())
				          .build();
	}

}
