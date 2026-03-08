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

import com.venky.validationservice.domain.model.ConfidenceLevel;
import com.venky.validationservice.domain.model.DomainDecision;
import com.venky.validationservice.domain.model.ValidationStatus;
import com.venky.validationservice.domain.service.DomainDecisionMapper;
import com.venky.validationservice.domain.service.EventFailureService;
import com.venky.validationservice.domain.service.ValidationDecisionService;
import com.venky.validationservice.exception.NonRetryableProviderException;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ProviderEventType;
import com.venky.validationservice.integration.common.ProviderValidationStatus;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventWorker {
	
	private final ValidationPersistenceService validationPersistence;
	private final ValidationDecisionService validationDecisionService;
	private final ProviderValidationResultService providerValidationResultService;
	private final ProviderValidationEventPersistenceService eventPersistenceService;
	private final Map<Provider, ProviderEventParser> parserMap;
	private final DomainDecisionMapper domainDecisionMapper;
	private final EventFailureService eventFailureService;

	public EventWorker(ValidationPersistenceService validationPersistence,
			ValidationDecisionService validationDecisionService,
			ProviderValidationResultService providerValidationResultService,
			ProviderValidationEventPersistenceService eventPersistenceService, List<ProviderEventParser> parsers, DomainDecisionMapper domainDecisionMapper, EventFailureService eventFailureService) {
		this.validationPersistence = validationPersistence;
		this.validationDecisionService = validationDecisionService;
		this.providerValidationResultService = providerValidationResultService;
		this.eventPersistenceService = eventPersistenceService;
		this.domainDecisionMapper = domainDecisionMapper;
		this.parserMap = parsers.stream()
				.collect(Collectors.toMap(ProviderEventParser::getProvider, Function.identity()));
		this.eventFailureService = eventFailureService;
	}

	@Scheduled(fixedDelay = 9000)
	public void processNextEvent() {
		Optional<ProviderValidationEventEntity> eventOpt = eventPersistenceService.fetchUnprocessedEvents(Instant.now());
		eventOpt.ifPresent(this::processSingleEvent);
	}


	@Transactional
	private void processSingleEvent(ProviderValidationEventEntity event) {

		try {
			
			log.info("Worker picked up validation event. eventId={}, valReqId={}, providerRefId={}, eventType={}", 
				    event.getId(), event.getValidationRequestId(), event.getProviderReferenceId(), event.getEventType());
			
			event.markProcessing();
			eventPersistenceService.save(event);

			Optional<ValidationRequestEntity> requestOpt = validationPersistence
					.findProviderReferenceId(event.getProviderReferenceId());

			if (requestOpt.isEmpty()) {
				eventFailureService.scheduleRetry(event, "Validation request not found yet");
				return;
			}

			ValidationRequestEntity request = requestOpt.get();

			// 2️⃣ Idempotency check
			if (request.isTerminal()) {
				event.markCompleted();
				event.setValidationRequestId(request.getValidationRequestId());
				eventPersistenceService.save(event);
				return;
			}

			ProviderEventParser parser = parserMap.get(event.getProvider());

			if (parser == null) {
				eventFailureService.markDeadLetter(event, "No parser found for provider");
				return;
			}
			
			if(event.getValidationRequestId() == null) {
				//For provider timed out & webhook cases where event don't have validation-requestIds  
				event.setValidationRequestId(request.getValidationRequestId());
			}
							
			ProviderResult providerResult;

			if (event.getEventType() == ProviderEventType.WEBHOOK) {
				providerResult = parser.parseWebhook(event.getRawPayload());
			} else {
				providerResult = parser.parseApiResponse(event.getRawPayload());
			}

			ProviderValidationStatus status = providerResult.getProviderStatus();

			if (ProviderValidationStatus.CREATED == status) {

				event.markCompleted();
				eventPersistenceService.save(event);
				
				request.setLastStatusCheckAt(Instant.now());
				validationPersistence.saveValidationEntity(request);
				return;
			}

			if (ProviderValidationStatus.FAILED == status) {

				request.markProviderFailed("PROVIDER_CANNOT_PROCESS_REQUEST");

				validationPersistence.saveValidationEntity(request);

				providerValidationResultService.store(request.getValidationRequestId(), event.getProvider().name(),
						providerResult.getProviderReferenceId(), providerResult);

				event.markCompleted();
				eventPersistenceService.save(event);

				return;
			}
			// here in completed state case all tables gets updated based on validation
			// results

			DomainDecision decision = validationDecisionService.decide(providerResult);
			
			log.info("Validation decision determined. valReqId={}, providerRefId={}, status={}, confidence={}", 
				    event.getValidationRequestId(), event.getProviderReferenceId(), 
				    decision.getDecision(), decision.getConfidence());

			ValidationStatus validationStatus = domainDecisionMapper.mapValidationStatus(decision.getDecision());

			ConfidenceLevel confidenceLevel = domainDecisionMapper.mapConfidenceLevel(decision.getConfidence());

			request.complete(validationStatus, confidenceLevel);

			validationPersistence.saveValidationEntity(request);
			
			event.markCompleted();
			eventPersistenceService.save(event);
			
			log.info("Successfully persisted final validation result. valReqId={}, providerRefId={}, finalStatus={}", 
					request.getValidationRequestId(),event.getProviderReferenceId(), providerResult.getProviderStatus());

			providerValidationResultService.store(request.getValidationRequestId(), event.getProvider().name(),
					event.getProviderReferenceId().toString(), providerResult);

		}

		catch (NonRetryableProviderException ex) {
			eventFailureService.markDeadLetter(event, "non retryable error in event worker");
		} catch (Exception ex) {
			eventFailureService.handleFailure(event, ex);
		}

	}

	

}
