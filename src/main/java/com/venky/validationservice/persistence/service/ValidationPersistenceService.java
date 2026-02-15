package com.venky.validationservice.persistence.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.exception.ValidationExecutionException;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.razorpay.RzpRequestFactory;
import com.venky.validationservice.persistence.entity.*;
import com.venky.validationservice.persistence.repository.*;

@Service
@Transactional
public class ValidationPersistenceService {

	private final ValidationRequestRepository requestRepo;

	public ValidationPersistenceService(ValidationRequestRepository requestRepo) {

		this.requestRepo = requestRepo;
	}

	public void createValidationRequest(UUID requestId) {
		ValidationRequestEntity entity = new ValidationRequestEntity(requestId, ExecutionStatus.INITIATED);
		requestRepo.save(entity);
	}

	public void markRequestPending(UUID requestId, String provider, String providerReferenceId) {
		
		ValidationRequestEntity entity = requestRepo.findById(requestId)
				.orElseThrow(() -> new ValidationExecutionException("Validation request ID not found" + requestId,
						FailureOrigin.INTERNAL_SYSTEM));

		entity.setProvider(provider);
		entity.setProviderReferenceId(providerReferenceId);
		entity.setExecutionStatus(ExecutionStatus.PENDING);
		entity.setUpdatedAt(java.time.Instant.now());

		requestRepo.save(entity);
	}

	 @Transactional(readOnly = true)
	public Optional<ValidationRequestEntity> findProviderReferenceId(String providerReferenceId) {
		return requestRepo.findByProviderReferenceId(providerReferenceId);
	}

	public void updateValidationEntity(ValidationRequestEntity request) {
		requestRepo.save(request);
	}

	 @Transactional(readOnly = true)
	public List<ValidationRequestEntity> findRequestsForPolling(String status, Instant threshold) {
		return requestRepo.findRequestsForPolling(status, threshold);
	}
	 
	public int markProcessingIfInitiated(UUID id) {
		return requestRepo.markProcessingIfInitiated(id);
	}

	@Transactional(readOnly = true)
	public Optional<ValidationRequestEntity> findByValidationRequestId(UUID validationRequestId) {
		// TODO Auto-generated method stub
		return requestRepo.findById(validationRequestId);
	}

}
