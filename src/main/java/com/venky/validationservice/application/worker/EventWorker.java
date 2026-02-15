package com.venky.validationservice.application.worker;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.venky.validationservice.domain.model.DomainDecision;
import com.venky.validationservice.domain.service.ValidationDecisionService;
import com.venky.validationservice.exception.NonRetryableProviderException;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ProviderEventType;
import com.venky.validationservice.integration.razorpay.RazorpayEventParser;
import com.venky.validationservice.integration.webhook.ProviderEventParser;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.repository.ProviderValidationEventRepository;
import com.venky.validationservice.persistence.repository.ValidationRequestRepository;
import com.venky.validationservice.persistence.service.ProviderValidationEventPersistenceService;
import com.venky.validationservice.persistence.service.ProviderValidationResultService;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

import jakarta.transaction.Transactional;

@Service
public class EventWorker {
	
	 private static final int MAX_RETRY = 5;
	 private static final Duration RETRY_DELAY = Duration.ofSeconds(4);

	private final ValidationPersistenceService validationPersistence;
	private final ValidationDecisionService validationDecisionService;
	private final ProviderValidationResultService providerValidationResultService;
	private final ProviderValidationEventPersistenceService eventPersistenceService;
	private final Map<Provider, ProviderEventParser> parserMap;

	public EventWorker(ValidationPersistenceService validationPersistence,
			ValidationDecisionService validationDecisionService,
			ProviderValidationResultService providerValidationResultService,
			ProviderValidationEventPersistenceService eventPersistenceService, List<ProviderEventParser> parsers) {
		this.validationPersistence = validationPersistence;
		this.validationDecisionService = validationDecisionService;
		this.providerValidationResultService = providerValidationResultService;
		this.eventPersistenceService = eventPersistenceService;

		this.parserMap = parsers.stream()
				.collect(Collectors.toMap(ProviderEventParser::getProvider, Function.identity()));
	}

	@Scheduled(fixedDelay = 2000)
	public void processNextEvent() {
		Optional<ProviderValidationEventEntity> eventOpt = eventPersistenceService.fetchUnprocessedEvents();
		eventOpt.ifPresent(this::processSingleEvent);
	}

	@Transactional
	private void processSingleEvent(ProviderValidationEventEntity event) {

		try {

			Optional<ValidationRequestEntity> requestOpt = validationPersistence
					.findProviderReferenceId(event.getProviderReferenceId());

			if (requestOpt.isEmpty()) {
				// Webhook before API response → retry later
				scheduleRetry(event, "Validation request not found yet");
				return;
			}

			ValidationRequestEntity request = requestOpt.get();

			// 2️⃣ Idempotency check
			if (request.isTerminal()) {
				event.markSkipped();
				eventPersistenceService.save(event);
				return;
			}
		
			ProviderEventParser parser = parserMap.get(event.getProvider());

			if (parser == null) {
				markFailed(event, "No parser found for provider");
				return;
			}

			ProviderResult providerResult;

			if (event.getEventType() == ProviderEventType.WEBHOOK) {
				providerResult = parser.parseWebhook(event.getRawPayload());
			} else {
				providerResult = parser.parseApiResponse(event.getRawPayload());
			}

			String status = providerResult.getProviderStatus();

			if ("CREATED".equalsIgnoreCase(status)) {

				// Still processing on provider side → skip
				event.markSkipped();
				eventPersistenceService.save(event);

				// update polling timestamp
				request.setLastStatusCheckAt(Instant.now());
				validationPersistence.updateValidationEntity(request);
				return;
			}

			if ("FAILED".equalsIgnoreCase(status)) {

				request.markProviderFailed();

				validationPersistence.updateValidationEntity(request);

				providerValidationResultService.store(request.getId(), event.getProvider().name(),
						providerResult.getProviderReferenceId(), providerResult);

				event.markCompleted();
				eventPersistenceService.save(event);

				return;
			}

			DomainDecision decision = validationDecisionService.decide(providerResult);

			request.complete(decision.getDecision(), decision.getConfidence());

			validationPersistence.updateValidationEntity(request);
			eventPersistenceService.markCompleted(event);

			providerValidationResultService.store(request.getId(), event.getProvider().name(),
					event.getProviderReferenceId().toString(), providerResult);

		}

		catch (NonRetryableProviderException ex) {
			markFailed(event, ex.getMessage());
		} catch (Exception ex) {
			handleFailure(event, ex);
		}

	}

	private void handleFailure(ProviderValidationEventEntity event, Exception ex) {

        int retry = event.getRetryCount() + 1;
        event.setRetryCount(retry);
        event.setLastError(ex.getMessage());

        if (retry >= MAX_RETRY) {
            event.markFailed();
        } else {
            event.markPending();
            event.setNextRetryAt(Instant.now().plus(RETRY_DELAY));
        }

        eventPersistenceService.save(event);
	}

	private void markFailed(ProviderValidationEventEntity event, String reason) {
		event.setLastError(reason);
		event.markFailed();
		eventPersistenceService.save(event);
	}

	private void scheduleRetry(ProviderValidationEventEntity event, String reason) {
		  event.setRetryCount(event.getRetryCount() + 1);
	      event.setLastError(reason);
	      event.setNextRetryAt(Instant.now().plus(RETRY_DELAY));
	      event.markPending();
	      eventPersistenceService.save(event);
	}

}
