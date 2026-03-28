package com.venky.validationservice.persistence.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.exception.ValidationExecutionException;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.razorpay.RzpRequestFactory;
import com.venky.validationservice.persistence.entity.*;
import com.venky.validationservice.persistence.repository.*;

@Service
//@Transactional
public class ValidationPersistenceService {

	private final ValidationRequestRepository requestRepo;

	public ValidationPersistenceService(ValidationRequestRepository requestRepo) {
		this.requestRepo = requestRepo;
	}

	public ValidationRequestEntity createValidationRequest(UUID requestId, String rawPayload, String incomingHash,Provider provider) {
		ValidationRequestEntity entity = new ValidationRequestEntity(requestId, ExecutionStatus.INITIATED,rawPayload,incomingHash,provider);
		requestRepo.save(entity);
		return entity;
	}

	 @Transactional(readOnly = true)
	public Optional<ValidationRequestEntity> findProviderReferenceId(String providerReferenceId) {
		return requestRepo.findByProviderReferenceId(providerReferenceId);
	}

	public void saveValidationEntity(ValidationRequestEntity request) {
		requestRepo.save(request);
	}

	 @Transactional(readOnly = true)
	public List<ValidationRequestEntity> findRequestsForPolling(ExecutionStatus processing, Instant threshold) {
		return requestRepo.findRequestsForPolling(processing, threshold,PageRequest.of(0, 100));
	}
	
	public int markInProcessingIfInitiated(UUID id, Instant callInitiatedAt) {
		return requestRepo.markInProcessingIfInitiated(id,callInitiatedAt);
	}

	@Transactional(readOnly = true)
	public Optional<ValidationRequestEntity> findByValidationRequestId(UUID validationRequestId) {
		return requestRepo.findById(validationRequestId);
	}
	
	public void markValidationRequestFailed(UUID validationRequestID,
			FailureOrigin failureOrigin,String message,Provider provider) {
			
		var requestEntity= getValidationRequest(validationRequestID);
		
		requestEntity.setExecutionStatus(ExecutionStatus.FAILED);
		
		if(provider != null)
		requestEntity.setProvider(provider);
		
		if (failureOrigin.equals(FailureOrigin.INTERNAL_SYSTEM))
			requestEntity.markValidationFailure(message);
		else
			requestEntity.markProviderFailed(message);

		requestRepo.save(requestEntity);
	}
	
	public List<Object[]> countPendingEvents(List<UUID> requestIds){
		return requestRepo.countPendingEvents(requestIds);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markProviderCallTimeout(UUID validationRequestID, Provider provider) {
		requestRepo.updateExecutionStatus(validationRequestID,ExecutionStatus.PROVIDER_CALL_TIMEOUT,provider);
	}

	public void updateValidationEventWithProvderDetails(UUID validationRequestId, Provider provider, String providerReferenceId) {
		ValidationRequestEntity entity = requestRepo.findById(validationRequestId)
				.orElseThrow(() -> new ValidationExecutionException("Validation request ID not found" + validationRequestId,
						FailureOrigin.INTERNAL_SYSTEM,validationRequestId));
		
		entity.setProvider(provider);
		entity.setProviderReferenceId(providerReferenceId);
		entity.setExecutionStatus(ExecutionStatus.PROCESSING);
		entity.setUpdatedAt(java.time.Instant.now());

		requestRepo.save(entity);
	}

	private ValidationRequestEntity getValidationRequest(UUID uuid) {
		Optional<ValidationRequestEntity> optRequestEntity = findByValidationRequestId(uuid);
		if(optRequestEntity.isEmpty())
			throw new IllegalStateException("validation_request_id is not present in DB ");
		return optRequestEntity.get();
	}

	public Optional<ValidationRequestEntity> findByIdempotencyKey(String idempotencyKey) {
		return requestRepo.findByIdempotencyKey(idempotencyKey);
	}
	
	
}
