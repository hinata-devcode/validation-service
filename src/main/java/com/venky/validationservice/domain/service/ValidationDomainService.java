package com.venky.validationservice.domain.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.venky.validationservice.domain.model.ConfidenceLevel;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationQueryResponse;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.exception.ThirdpartyProviderException;
import com.venky.validationservice.exception.ValidationExecutionException;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.ValidationExecutionResult;
import com.venky.validationservice.integration.common.ValidationState;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

@Service
public class ValidationDomainService {

	private final ProviderValidationPort providerPort;
	private final ValidationPersistenceService validationPersistenceService;
	private ValidationRequestEntity validationRequestEntity;

	public ValidationDomainService(ProviderValidationPort providerPort, ValidationPersistenceService validationPersistenceService) {
		this.providerPort = providerPort;
		this.validationPersistenceService = validationPersistenceService;
	}

	public ValidationExecutionResult validate(FundAccountDetails details, ValidationState validationState) {
		try {

			// Call provider
			
			validationRequestEntity=validationPersistenceService.createValidationRequest(validationState.getValidationRequestId());
			
			//ATOMIC UPDATE IN CASE OF MUTIPLE INSTACNES OR TWO THREADS TRYING TO UPDATE SAME REQUEST
			 int updated = validationPersistenceService.markProcessingIfInitiated(validationState.getValidationRequestId());
			 
			 if(updated==0)
				 throw new IllegalStateException("Request already processing");
			
			ValidationExecutionResult execution = providerPort.validate(details, validationState);

			return execution;

		}

		catch (ThirdpartyProviderException ex) {
			validationPersistenceService.markValidationRequestFailed(validationRequestEntity,FailureOrigin.EXTERNAL_PROVIDER,"PROVIDER_ERROR");
			throw new ValidationExecutionException("Validation could not be initiated", FailureOrigin.EXTERNAL_PROVIDER,
					ex);

		} catch (RuntimeException ex) {
			validationPersistenceService.markValidationRequestFailed(validationRequestEntity, FailureOrigin.INTERNAL_SYSTEM,"INTERNAL_ERROR");
			throw new ValidationExecutionException("Internal validation error", FailureOrigin.INTERNAL_SYSTEM, ex);
		}
	}

	public ValidationQueryResponse  fetchResults(UUID validationRequestId) {
		var validationResult = validationPersistenceService.findByValidationRequestId(validationRequestId);
		
		if(validationResult.isEmpty())
			throw new ValidationExecutionException(
                    "Validation request not found: " + validationRequestId, FailureOrigin.INTERNAL_SYSTEM);
		
		ValidationResult result = null;
		
		var validationEntity = validationResult.get();
		
		 if (validationEntity.getExecutionStatus() == ExecutionStatus.COMPLETED) {
	            result = ValidationResult.builder()
	                    .status(validationEntity.getValidationStatus())
	                    .confidenceLevel(validationEntity.getConfidenceLevel())
	                    .build();
	        }
		
		 return ValidationQueryResponse.builder()
	                .validationRequestId(validationEntity.getValidationRequestId())
	                .executionStatus(validationEntity.getExecutionStatus())
	                .result(result)
	                .build();
	}
	
	

}
