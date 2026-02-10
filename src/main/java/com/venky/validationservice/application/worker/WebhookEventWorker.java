package com.venky.validationservice.application.worker;

import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.venky.validationservice.domain.model.DomainDecision;
import com.venky.validationservice.domain.service.ValidationDecisionService;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.razorpay.RazorpayWebhookParser;
import com.venky.validationservice.integration.razorpay.RazorpayWebhookResult;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.repository.ProviderValidationEventRepository;
import com.venky.validationservice.persistence.repository.ValidationRequestRepository;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

import jakarta.transaction.Transactional;

@Service
public class WebhookEventWorker {

	private final ProviderValidationEventRepository eventRepo;
	private final ValidationRequestRepository validationRepo;
	private final ValidationPersistenceService validationPersistence;
	private final RazorpayWebhookParser razorpayWebhookParser;
	private final ValidationDecisionService validationDecisionService;

	public WebhookEventWorker(ProviderValidationEventRepository eventRepo, ValidationRequestRepository validationRepo,
			ValidationPersistenceService validationPersistence, RazorpayWebhookParser razorpayWebhookParser,
			ValidationDecisionService validationDecisionService) {
		this.eventRepo = eventRepo;
		this.validationRepo = validationRepo;
		this.validationPersistence = validationPersistence;
		this.razorpayWebhookParser = razorpayWebhookParser;
		this.validationDecisionService = validationDecisionService;
	}

	@Scheduled(fixedDelay = 2000)
	public void processNextEvent() {
		Optional<ProviderValidationEventEntity> eventOpt = eventRepo.findNextUnprocessed();

		eventOpt.ifPresent(this::processSingleEvent);
	}

	@Transactional
	private void processSingleEvent(ProviderValidationEventEntity event) {

		Optional<ValidationRequestEntity> requestOpt = validationRepo
				.findByProviderReferenceId(event.getProviderReferenceId());

		if (requestOpt.isEmpty()) {
			// Webhook before API response → retry later
			return;
		}

		ValidationRequestEntity request = requestOpt.get();

		// 2️⃣ Idempotency check
		if (request.getExecutionStatus() != ExecutionStatus.PENDING.toString()) {
			event.markProcessed();
			return;
		}

		RazorpayWebhookResult providerResult = razorpayWebhookParser.parse(event.getRawPayload());

		DomainDecision decision = validationDecisionService.decide(providerResult);

		request.complete(decision.getDecision(), decision.getConfidence());

		validationPersistence.save(request);
		event.markProcessed();

	}

}
