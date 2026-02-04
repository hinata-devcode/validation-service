package com.venky.validationservice.integration.razorpay;

import java.util.Map;

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
	private String referenceId;
	private Map<String, String> notes;
}
