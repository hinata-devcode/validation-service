package com.venky.validationservice.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VpaRequestDTO {

	@JsonProperty("vpa_address")
	@NotBlank(message = "VPA is mandatory")
	// Regex for standard UPI ID (e.g., venky@oksbi)
	//@Pattern(regexp = "^[\\w.-]+@[\\w.-]+$", message = "Invalid UPI ID format")
	private String vpa;

	@Override
	public String toString() {
		// We mask everything before the '@' symbol
		return "VpaRequest{vpa='" + maskVpa(vpa) + "'}";
	}

	private String maskVpa(String input) {
		if (input == null || !input.contains("@"))
			return "REDACTED";
		String[] parts = input.split("@");
		// Result: XXXX@oksbi
		return "XXXX@" + parts[1];
	}

}