package com.venky.validationservice.integration.razorpay;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.venky.validationservice.application.polling.ProviderPollingService;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ProviderEventType;
import com.venky.validationservice.persistence.entity.EventExecutionStatus;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.service.ProviderValidationEventPersistenceService;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

@Service
public class RazorpayPollingService implements ProviderPollingService {

	private final RazorpayClient razorpayClient;
	private final ProviderValidationEventPersistenceService eventPersistence;
	private final ValidationPersistenceService validationPersistenceService;
	private final int maxPollingAttempts = 5;

	public RazorpayPollingService(RazorpayClient razorpayClient,
			ProviderValidationEventPersistenceService eventPersistence,
			ValidationPersistenceService validationPersistenceService) {
		super();
		this.razorpayClient = razorpayClient;
		this.eventPersistence = eventPersistence;
		this.validationPersistenceService = validationPersistenceService;
	}

	@Override
	public Provider getProvider() {
		return Provider.RAZORPAY;
	}

	@Transactional
	@Override
	public void poll(ValidationRequestEntity request) {

		if (request.isTerminal()) {
			return;
		}

		String apiResponse = razorpayClient.fetchStatus(request.getProviderReferenceId());

		request.incrementPollAttempts();
		request.updateLastStatusCheck();

		if (request.isPollingTimedOut(Duration.ofMinutes(30), maxPollingAttempts)) {
			request.markProviderFailed("RETRIES_EXHAUSTED");
			validationPersistenceService.saveValidationEntity(request);
			return;
		}

		// here we are saving the info to audit table
		ProviderValidationEventEntity event = new ProviderValidationEventEntity(request.getValidationRequestId(), Provider.RAZORPAY,
				request.getProviderReferenceId(), ProviderEventType.API_RESPONSE, apiResponse);

		event.markPending();
		event.setRetryCount(0);
		eventPersistence.save(event);
		request.setLastStatusCheckAt(Instant.now());

		validationPersistenceService.saveValidationEntity(request);
	}

}
