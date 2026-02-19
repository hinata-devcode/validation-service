package com.venky.validationservice.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.Getter;

@Getter
public class BankValidationRequest {

	@JsonProperty("bank_account")
    @Valid
    private BankAccountRequestDTO bankAccount;

	@JsonProperty("user_details")
    @Valid
    private UserDetailsDTO userDetails;

   
}
