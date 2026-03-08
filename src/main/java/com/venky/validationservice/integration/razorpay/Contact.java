package com.venky.validationservice.integration.razorpay;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contact {
	private String name;
	private String email;
	private String contact;
	private String type;
	@JsonProperty("reference_id")
	private String referenceId;
	private Map<String, String> notes;
}
