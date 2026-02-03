package com.venky.validationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.venky.validationservice.application.ValidationApplicationService;
import com.venky.validationservice.application.ValidationResponseDTO;
import com.venky.validationservice.controller.dto.BankAccountRequestDTO;
import com.venky.validationservice.controller.dto.UserDetailsDTO;
import com.venky.validationservice.controller.dto.VpaRequestDTO;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.domain.service.ProviderValidationPort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/validation")
@RequiredArgsConstructor
public class ValidationController {

	private final ValidationApplicationService validationService;

	@PostMapping("/bank-account")
	// @Valid triggers the Gatekeeper (DTO) automatically
	public ResponseEntity<ValidationResponseDTO> validateBank(@Valid @RequestBody BankAccountRequestDTO request, @Valid @RequestBody UserDetailsDTO userDetails) {

		// If we reach here, the data is safe and valid format.
		ValidationResponseDTO response = validationService.validateBankAccount(request,userDetails);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/vpa")
    public ResponseEntity<ValidationResult> validateVpa(
            @Valid @RequestBody VpaRequestDTO request,@Valid @RequestBody UserDetailsDTO detailsDTO) {
    
        ValidationResult response = validationService.validateVpa(request,detailsDTO);  
        return ResponseEntity.ok(response);
    }

}
