package com.venky.validationservice.persistence.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

import com.venky.validationservice.persistence.entity.*;
import com.venky.validationservice.persistence.repository.*;

@Service
public class ValidationPersistenceService {

    private final ValidationRequestRepository requestRepo;
    private final ProviderValidationEventRepository eventRepo;

    public ValidationPersistenceService(
            ValidationRequestRepository requestRepo,
            ProviderValidationEventRepository eventRepo) {

        this.requestRepo = requestRepo;
        this.eventRepo = eventRepo;
    }

    /* ---------- Validation request ---------- */

    public void createValidationRequest(UUID requestId) {
        ValidationRequestEntity entity =
                new ValidationRequestEntity(requestId, "INITIATED");

        requestRepo.save(entity);
    }

    public void markRequestPending(
            UUID requestId,
            String provider,
            String providerReferenceId) {

        ValidationRequestEntity entity =
                requestRepo.findById(requestId)
                        .orElseThrow();

        entity.setProvider(provider);
        entity.setProviderReferenceId(providerReferenceId);
        entity.setExecutionStatus("PENDING");
        entity.setUpdatedAt(java.time.Instant.now());

        requestRepo.save(entity);
    }

	public void save(ValidationRequestEntity request) {
		 requestRepo.save(request);
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
