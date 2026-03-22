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

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		
		log.info("Polling Razorpay for validation status. valReqId={}, providerRefId={}", 
				request.getValidationRequestId(), request.getProviderReferenceId());
		long startTime = System.currentTimeMillis();
		
		String apiResponse;
		try {
			apiResponse = razorpayClient.fetchStatus(request.getProviderReferenceId());
		} catch (CallNotPermittedException ex) {	
			log.warn("Circuit Breaker is OPEN. Skipping polling for valReqId={} this cycle. Will retry next schedule.", 
					request.getValidationRequestId());
			return;
		} catch (Exception ex) {
            log.error("Error polling Razorpay for valReqId={}: {}", request.getValidationRequestId(), ex.getMessage());
            return;
        }
		
		 long executionTime = System.currentTimeMillis() - startTime;
		 	 
	    log.info("Razorpay polling successful. valReqId={}, providerRefId={}, executionTime={}ms, status={}", 
	    		request.getValidationRequestId(), request.getProviderReferenceId(), executionTime); // e.g. "completed" or "failed"
	   
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
		 log.info("raw api respponse is directly inserted into provider_validation_event DB call");
		request.setLastStatusCheckAt(Instant.now());

		validationPersistenceService.saveValidationEntity(request);
		 log.info("validation request entity is updated wih lastStatusCheckAt "+request.getLastStatusCheckAt());
	}

}
