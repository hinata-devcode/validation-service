package com.venky.validationservice.application;

import java.util.Optional;
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
import com.venky.validationservice.exception.IdempotencyConflictException;
import com.venky.validationservice.integration.common.ValidationState;
import com.venky.validationservice.integration.utils.HashUtil;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidationApplicationService {

	// DEPENDENCY INJECTION of the Interface, not the Class
	//private final FundAccountValidationService validationService;
	private final ValidationDomainService domainService;
	private final ValidationRequestIdGenerator uuidGenerator;
	private final ValidationPersistenceService validationPersistenceService;
	
	 public ValidationApplicationService(ValidationDomainService domainService, ValidationRequestIdGenerator uuidGenerator, ValidationPersistenceService validationPersistenceService) {
	        this.domainService = domainService;
			this.uuidGenerator = uuidGenerator;
			this.validationPersistenceService = validationPersistenceService;
	    }

	  public ValidationResponseDTO validateBankAccount(
	            BankAccountRequestDTO bankDto,
	            UserDetailsDTO userDto, String idempotencyKey) {
		  
		  
		  String rawPayload = "BANK|" + bankDto.getAccountNumber() + "|" + bankDto.getIfsc();
		  String incomingHash = HashUtil.generateSha256(rawPayload);
		  
		  Optional<ValidationRequestEntity> existingRequestOpt = validationPersistenceService.findByIdempotencyKey(idempotencyKey);
		  
		  if (existingRequestOpt.isPresent()) {
			  ValidationRequestEntity savedEntity = existingRequestOpt.get();
		        
		        // 3. The Payload Hash Check
		        if (savedEntity.getPayloadHash().equals(incomingHash)) {
		            log.info("Idempotent retry detected for Client Key: {}", idempotencyKey);
		            
		            ValidationState validationState = new ValidationState(savedEntity.getValidationRequestId(),savedEntity.getPayloadHash(),savedEntity.getIdempotencyKey());
		            return ValidationResponseDTO.builder()
			                .validationRequestId(validationState.getValidationRequestId()) 
			                .executionStatus(validationState.getExecutionStatus())
			                .build();
		        } else {
		            log.warn("Idempotency conflict! Client Key {} used with different payload.", idempotencyKey);
		            throw new IdempotencyConflictException("This Idempotency-Key was already used for a different request.");
		        }
		    }
		 	  
		  UUID requestId = uuidGenerator.generate();
		  
		  ValidationState validationState =
		            new ValidationState(requestId,incomingHash,idempotencyKey);
		  
		  log.info("Received bank validation request. Assigned validationRequestId: {}", requestId);
		  
	        // Build domain input
	        FundAccountDetails fundAccountdetails = FundAccountDetails.builder()
	                .beneficiaryName(userDto.getName())
	                .email(userDto.getEmail())
	                .phone(userDto.getPhone())
	                .accountNumber(bankDto.getAccountNumber())
	                .ifsc(bankDto.getIfsc())
	                .build();
	        
	        log.info("Initiating bank account validation. valReqId={}, ifsc={}", 
	        	    validationState.getValidationRequestId(), fundAccountdetails.getIfsc()); 
	        
	      return  domainService.validate(fundAccountdetails, validationState);
	            
	    }

	public ValidationResponseDTO validateVpa(@Valid VpaRequestDTO vpaDto, @Valid UserDetailsDTO userDto, String idempotencyKey) {
		// TODO Auto-generated method stub
		
		 String rawPayload = "VPA" + vpaDto.getVpa();
		 String incomingHash = HashUtil.generateSha256(rawPayload);
		  
		 Optional<ValidationRequestEntity> existingRequestOpt = validationPersistenceService.findByIdempotencyKey(idempotencyKey);
		 
		 if (existingRequestOpt.isPresent()) {
			  ValidationRequestEntity savedEntity = existingRequestOpt.get();
		        
		        // 3. The Payload Hash Check
		        if (savedEntity.getPayloadHash().equals(incomingHash)) {
		            log.info("Idempotent retry detected for Client Key: {}", idempotencyKey);
		            
		            ValidationState validationState = new ValidationState(savedEntity.getValidationRequestId(),savedEntity.getPayloadHash(),savedEntity.getIdempotencyKey());
		            validationState.setProvider(savedEntity.getProvider());
		            return ValidationResponseDTO.builder()
			                .validationRequestId(validationState.getValidationRequestId()) 
			                .executionStatus(validationState.getExecutionStatus())
			                .build();
		        } else {
		            log.warn("Idempotency conflict! Client Key {} used with different payload.", idempotencyKey);
		            throw new IdempotencyConflictException("This Idempotency-Key was already used for a different request.");
		        }
		    }
		  
		 UUID requestId = uuidGenerator.generate();
		  
		  ValidationState validationState =
		            new ValidationState(requestId,incomingHash,idempotencyKey);
		  
		  log.info("Received vpa validation request. Assigned validationRequestId: {}", requestId);
		  	
		 FundAccountDetails fundAccountdetails = FundAccountDetails.builder()
	                .beneficiaryName(userDto.getName())
	                .email(userDto.getEmail())
	                .phone(userDto.getPhone())
	                .vpa(vpaDto.getVpa())
	                .build();

		 log.info("Initiating VPA validation. valReqId={}",  validationState.getValidationRequestId()); 
		 
		return domainService.validate(fundAccountdetails, validationState);
		 	
	}

	public ValidationQueryResponse getValidation(UUID validationRequestId) {
		return domainService.fetchResults(validationRequestId);
	}
}
