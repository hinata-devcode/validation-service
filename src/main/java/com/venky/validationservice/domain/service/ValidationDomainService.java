package com.venky.validationservice.domain.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationQueryResponse;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.exception.ProviderCallTimeoutException;
import com.venky.validationservice.exception.ThirdpartyProviderException;
import com.venky.validationservice.exception.ValidationExecutionException;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.ValidationExecutionResult;
import com.venky.validationservice.integration.common.ValidationState;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

			validationRequestEntity = validationPersistenceService
					.createValidationRequest(validationState.getValidationRequestId());

			Instant callInitiatedAt = Instant.now();

			// ATOMIC UPDATE IN CASE OF MUTIPLE INSTACNES OR TWO THREADS TRYING TO UPDATE
			// SAME REQUEST & UPDATING PROVIDER CALL INITIATED AT
			int updated = validationPersistenceService
					.markInProcessingIfInitiated(validationState.getValidationRequestId(), callInitiatedAt);

			if (updated == 0) {
				log.warn("Concurrent update attempt blocked for validationRequestId: {}",
						validationState.getValidationRequestId());
				throw new IllegalStateException("Request already processing");
			}

			log.info("Successfully locked validationRequestId: {} for processing. Initiating provider call.",
					validationState.getValidationRequestId());

			ValidationExecutionResult execution = providerPort.validate(details, validationState);

			return execution;

		}

		catch (ProviderCallTimeoutException ex) {
			var provider = ex.getProvider();
			log.warn("Provider call timed out for validationRequestId: {}, Provider: {}",
					validationRequestEntity.getValidationRequestId(), provider, ex);
			validationPersistenceService.markProviderCallTimeout(validationRequestEntity.getValidationRequestId(),
					provider);
			throw new ValidationExecutionException("Provider call uncertain", FailureOrigin.EXTERNAL_PROVIDER, ex, validationRequestEntity.getValidationRequestId());
		} catch (ThirdpartyProviderException ex) {
			log.error("Provider rejected validationRequestId: {}. Provider: {}",
					validationRequestEntity.getValidationRequestId(), ex.getProvider(), ex);
			validationPersistenceService.markValidationRequestFailed(validationRequestEntity.getValidationRequestId(),
					FailureOrigin.EXTERNAL_PROVIDER, "PROVIDER_ERROR", ex.getProvider());
			throw new ValidationExecutionException("Validation could not be initiated", FailureOrigin.EXTERNAL_PROVIDER,
					ex,validationRequestEntity.getValidationRequestId());

		} catch (RuntimeException ex) {
			log.error("Internal validation error for validationRequestId: {}",
					validationRequestEntity.getValidationRequestId(), ex);
			validationPersistenceService.markValidationRequestFailed(validationRequestEntity.getValidationRequestId(),
					FailureOrigin.INTERNAL_SYSTEM, "INTERNAL_ERROR", validationRequestEntity.getProvider());
			throw new ValidationExecutionException("Internal validation error", FailureOrigin.INTERNAL_SYSTEM, ex,validationRequestEntity.getValidationRequestId());
		}
	}

	public ValidationQueryResponse  fetchResults(UUID validationRequestId) {
		var validationResult = validationPersistenceService.findByValidationRequestId(validationRequestId);
		
		if(validationResult.isEmpty())
			throw new ValidationExecutionException(
                    "Validation request not found: " + validationRequestId, FailureOrigin.INTERNAL_SYSTEM,validationRequestId);
		
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
