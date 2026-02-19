package com.venky.validationservice.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public class VpaValidationRequest {
	
	@JsonProperty("vpa")
    @Valid
    private VpaRequestDTO vpaRequestDTO;

	@JsonProperty("user_details")
    @Valid
    private UserDetailsDTO userDetails;


}
