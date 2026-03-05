package com.venky.validationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/validation")
@RequiredArgsConstructor
@Slf4j
public class ValidationController {

	private final ValidationApplicationService validationService;

	@PreAuthorize("hasAnyRole('USER', 'ADMIN')") 
	@PostMapping("/bank-account")
	public ResponseEntity<ValidationResponseDTO> validateBank(
			@RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
			@Valid @RequestBody BankValidationRequest request) {
		
		if (idempotencyKey!=null && idempotencyKey.length() > 36) {
		    throw new IllegalArgumentException("Idempotency-Key must not exceed 36 characters");
		}
		
		BankAccountRequestDTO bankAccount = request.getBankAccount();
		UserDetailsDTO userDetails = request.getUserDetails();

		ValidationResponseDTO response = validationService.validateBankAccount(bankAccount, userDetails,
				idempotencyKey);
		return ResponseEntity.ok(response);
	}
	
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')") 
	@PostMapping("/vpa")
	public ResponseEntity<ValidationResponseDTO> validateVpa(
			@RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
			@Valid @RequestBody VpaValidationRequest request) {
		
		if (idempotencyKey!=null && idempotencyKey.length() > 36) {
		    throw new IllegalArgumentException("Idempotency-Key must not exceed 36 characters");
		}

		VpaRequestDTO vpaDetails = request.getVpaRequestDTO();
		UserDetailsDTO userDetails = request.getUserDetails();

		ValidationResponseDTO response = validationService.validateVpa(vpaDetails, userDetails, idempotencyKey);
		return ResponseEntity.ok(response);
	}
	
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')") 
	 @GetMapping("/{validationRequestId}")
	    public ResponseEntity<ValidationQueryResponse> getValidation(
				@PathVariable UUID validationRequestId) {

			log.info("Received validation status request for id={}", validationRequestId);

			ValidationQueryResponse response = validationService.getValidation(validationRequestId);

			return ResponseEntity.ok(response);
		}
	 
	 
	 @GetMapping("/analytics/all")
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<?> fetchAllValidations() {
	        
	        // In a real app, this would call your Service -> Repository -> DB
	        // For now, returning a mock list for the structure
	        List<Map<String, String>> history = List.of(
	            Map.of("trackingId", "uuid-1", "user", "venky", "status", "COMPLETED"),
	            Map.of("trackingId", "uuid-2", "user", "bob", "status", "FAILED")
	        );

	        return ResponseEntity.ok(Map.of(
	            "totalCount", history.size(),
	            "data", history
	        ));
	    }

}
