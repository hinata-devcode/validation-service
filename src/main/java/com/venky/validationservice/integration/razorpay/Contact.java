package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Contact {
	private String name;
	private String email;
	private String contact;
	private String type;
	@JsonProperty("reference_id")
	private String referenceId;
}
