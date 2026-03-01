package com.venky.validationservice.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BankValidationRequest {

	@JsonProperty("bank_account")
    @Valid
    @NotNull(message="Bank details are required")
    private BankAccountRequestDTO bankAccount;

	@JsonProperty("user_details")
    @Valid
    @NotNull(message="User details are required")
    private UserDetailsDTO userDetails;

   
}
