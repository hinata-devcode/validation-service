package com.venky.validationservice.persistence.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.venky.validationservice.integration.razorpay.RzpRequestFactory;
import com.venky.validationservice.persistence.entity.*;
import com.venky.validationservice.persistence.repository.*;

@Service
public class ValidationPersistenceService {

    private final RzpRequestFactory rzpRequestFactory;

	private final ValidationRequestRepository requestRepo;

	public ValidationPersistenceService(ValidationRequestRepository requestRepo, RzpRequestFactory rzpRequestFactory) {

		this.requestRepo = requestRepo;

		this.rzpRequestFactory = rzpRequestFactory;

	}

	/* ---------- Validation request ---------- */

	public void createValidationRequest(UUID requestId) {
		ValidationRequestEntity entity = new ValidationRequestEntity(requestId, "INITIATED");

		requestRepo.save(entity);
	}

	public void markRequestPending(UUID requestId, String provider, String providerReferenceId) {

		ValidationRequestEntity entity = requestRepo.findById(requestId).orElseThrow();

		entity.setProvider(provider);
		entity.setProviderReferenceId(providerReferenceId);
		entity.setExecutionStatus("PENDING");
		entity.setUpdatedAt(java.time.Instant.now());

		requestRepo.save(entity);
	}

	public Optional<ValidationRequestEntity> findProviderReferenceId(String providerReferenceId) {
		return requestRepo.findByProviderReferenceId(providerReferenceId);
	}

	public void updateValidationEntity(ValidationRequestEntity request) {
		requestRepo.save(request);
	}

	public List<ValidationRequestEntity> findRequestsForPolling(String status, Instant threshold) {
		return requestRepo.findRequestsForPolling(status, threshold);
	}

	/* ---------- Provider events ---------- */

//    public void saveApiResponseEvent(
//            UUID requestId,
//            String provider,
//            String providerReferenceId,
//            String rawPayload) {
//
//        ProviderValidationEventEntity event =
//                new ProviderValidationEventEntity(
//                        provider,
//                        providerReferenceId,
//                        "API_RESPONSE",
//                        rawPayload
//                );
//
//        event.setValidationRequestId(requestId);
//        eventRepo.save(event);
//    }
}
