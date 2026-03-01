package com.venky.validationservice.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.venky.validationservice.controller.dto.BankAccountRequestDTO;
import com.venky.validationservice.controller.dto.UserDetailsDTO;
import com.venky.validationservice.controller.dto.VpaRequestDTO;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationQueryResponse;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.domain.service.ProviderValidationPort;
import com.venky.validationservice.domain.service.ValidationDomainService;
import com.venky.validationservice.integration.common.ValidationExecutionResult;
import com.venky.validationservice.integration.common.ValidationState;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;

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
	        
	        log.info("Initiating bank account validation. valReqId={}, ifsc={}", 
	        	    validationState.getValidationRequestId(), details.getIfsc()); 

	        ValidationExecutionResult executionResult =
	                domainService.validate(details,validationState);

	        // Build API response
	        return ValidationResponseDTO.from(executionResult);
	    }

	public ValidationResponseDTO validateVpa(@Valid VpaRequestDTO vpaDto, @Valid UserDetailsDTO userDto, ValidationState validationState) {
		// TODO Auto-generated method stub
		
		 FundAccountDetails details = FundAccountDetails.builder()
	                .beneficiaryName(userDto.getName())
	                .email(userDto.getEmail())
	                .phone(userDto.getPhone())
	                .vpa(vpaDto.getVpa())
	                .build();

		 log.info("Initiating VPA validation. valReqId={}", validationState.getValidationRequestId()); 
	        ValidationExecutionResult executionResult =
	                domainService.validate(details,validationState);

	        // Build API response
	        return ValidationResponseDTO.from(executionResult);
	}

	public ValidationQueryResponse getValidation(UUID validationRequestId) {
		return domainService.fetchResults(validationRequestId);
	}
}
