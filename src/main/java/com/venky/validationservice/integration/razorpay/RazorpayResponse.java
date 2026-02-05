package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayResponse {
	private String validationId; // The "fav_..." ID
	private String validationStatus;
}