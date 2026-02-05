package com.venky.validationservice.application;

import org.springframework.stereotype.Service;

import com.venky.validationservice.controller.dto.BankAccountRequestDTO;
import com.venky.validationservice.controller.dto.UserDetailsDTO;
import com.venky.validationservice.controller.dto.VpaRequestDTO;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.domain.service.ProviderValidationPort;
import com.venky.validationservice.domain.service.ValidationDomainService;
import com.venky.validationservice.integration.common.ValidationExecutionResult;
import com.venky.validationservice.integration.common.ValidationState;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidationApplicationService {

	// DEPENDENCY INJECTION of the Interface, not the Class
	//private final FundAccountValidationService validationService;
	private final ValidationDomainService domainService;
	
	 public ValidationApplicationService(ValidationDomainService domainService) {
	        this.domainService = domainService;
	    }

	  public ValidationResponseDTO validateBankAccount(
	            BankAccountRequestDTO bankDto,
	            UserDetailsDTO userDto, ValidationState validationState) {

	        // Build domain input
	        FundAccountDetails details = FundAccountDetails.builder()
	                .beneficiaryName(userDto.getName())
	                .email(userDto.getEmail())
	                .phone(userDto.getPhone())
	                .accountNumber(bankDto.getAccountNumber())
	                .ifsc(bankDto.getIfsc())
	                .build();

	        ValidationExecutionResult executionResult =
	                domainService.validate(details,validationState);

	        // Build API response
	        return new ValidationResponseDTO(executionResult);
	    }

	public ValidationResult validateVpa(@Valid VpaRequestDTO request, @Valid UserDetailsDTO detailsDTO) {
		// TODO Auto-generated method stub
		return null;
	}
}
