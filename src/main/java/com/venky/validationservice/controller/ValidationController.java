package com.venky.validationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import com.venky.validationservice.application.ValidationApplicationService;
import com.venky.validationservice.application.ValidationRequestIdGenerator;
import com.venky.validationservice.application.ValidationResponseDTO;
import com.venky.validationservice.controller.dto.BankAccountRequestDTO;
import com.venky.validationservice.controller.dto.BankValidationRequest;
import com.venky.validationservice.controller.dto.UserDetailsDTO;
import com.venky.validationservice.controller.dto.VpaRequestDTO;
import com.venky.validationservice.controller.dto.VpaValidationRequest;
import com.venky.validationservice.domain.model.ValidationQueryResponse;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.domain.service.ProviderValidationPort;
import com.venky.validationservice.integration.common.ValidationState;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/validation")
@RequiredArgsConstructor
public class ValidationController {

	private final ValidationApplicationService validationService;
	private final ValidationRequestIdGenerator uuidGenerator;

	@PostMapping("/bank-account")
	// @Valid triggers the Gatekeeper (DTO) automatically
	public ResponseEntity<ValidationResponseDTO> validateBank( @Valid @RequestBody BankValidationRequest request) {

		UUID requestId = uuidGenerator.generate();
		
		BankAccountRequestDTO bankAccount = request.getBankAccount();
	    UserDetailsDTO userDetails = request.getUserDetails();

		  ValidationState validationState =
		            new ValidationState(requestId);
		// If we reach here, the data is safe and valid format.
		ValidationResponseDTO response = validationService.validateBankAccount(bankAccount,userDetails,validationState);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/vpa")
	public ResponseEntity<ValidationResponseDTO> validateVpa(@Valid @RequestBody VpaValidationRequest request) {

		UUID requestId = uuidGenerator.generate();

		VpaRequestDTO vpaDetails = request.getVpaRequestDTO();
		UserDetailsDTO userDetails = request.getUserDetails();

		ValidationState validationState = new ValidationState(requestId);

		ValidationResponseDTO response = validationService.validateVpa(vpaDetails, userDetails, validationState);
		return ResponseEntity.ok(response);
	}
	
	 @GetMapping("/{validationRequestId}")
	    public ResponseEntity<ValidationQueryResponse> getValidation(
	            @PathVariable UUID validationRequestId) {

	        //log.info("Received validation status request for id={}", validationRequestId);

	        ValidationQueryResponse response =
	        		validationService.getValidation(validationRequestId);

	        return ResponseEntity.ok(response);
	    }

}
