package com.venky.validationservice.integration.razorpay;

import com.venky.validationservice.domain.model.FundAccountDetails;

public class VpaMapper {
	
	public static Vpa createVpa(FundAccountDetails accountDetails) {
		return Vpa.builder()
				          .address(accountDetails.getVpa())
				          .build();
	}

}
