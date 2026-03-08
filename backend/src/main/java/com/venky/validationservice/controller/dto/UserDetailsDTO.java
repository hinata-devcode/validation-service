package com.venky.validationservice.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDetailsDTO {
	@NotBlank(message = "Name is required")
	private String name;
	private String email;
	private String phone;

	@Override
	public String toString() {
		return "UserDetails{name='REDACTED', email='REDACTED'}";
	}

}
