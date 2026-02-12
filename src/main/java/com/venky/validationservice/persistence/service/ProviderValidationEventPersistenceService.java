package com.venky.validationservice.persistence.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ProviderEventType;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;
import com.venky.validationservice.persistence.repository.ProviderValidationEventRepository;

@Service
public class ProviderValidationEventPersistenceService {

	private final ProviderValidationEventRepository repository;

	public ProviderValidationEventPersistenceService(ProviderValidationEventRepository repository) {
		this.repository = repository;
	}

	// API response logging
	public void recordApiResponse(UUID validationRequestId, Provider provider, String providerReferenceId,
			String rawPayload) {
		ProviderValidationEventEntity event = new ProviderValidationEventEntity(validationRequestId, provider,
				providerReferenceId, ProviderEventType.API_RESPONSE, rawPayload);

		repository.save(event);
	}

	// Webhook logging
	public void recordWebhookEvent(Provider provider, String providerReferenceId, String rawPayload) {
		ProviderValidationEventEntity event = new ProviderValidationEventEntity(null, // may not know UUID yet
				provider, providerReferenceId, ProviderEventType.WEBHOOK, rawPayload);

		repository.save(event);
	}

	public Optional<ProviderValidationEventEntity> fetchUnprocessedEvents() {
		return repository.findNextUnprocessed();
	}

	public void markCompleted(ProviderValidationEventEntity event) {
		event.markCompleted();
		repository.save(event);
	}

	public void save(ProviderValidationEventEntity event) {
		repository.save(event);
	}

//	public void markSkipped(ProviderValidationEventEntity event) {
//		event.markSkipped();
//		repository.save(event);
//	}
//	
//	public void markProcessing(ProviderValidationEventEntity event) {
//		event.markProcessing();
//		repository.save(event);
//	}
//	
//	public void markFailed(ProviderValidationEventEntity event) {
//		event.markFailed();
//		repository.save(event);
//	}
	
}
