package com.venky.validationservice.domain.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.venky.validationservice.application.RetryPolicy;
import com.venky.validationservice.application.worker.RetryPolicyResolver;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;
import com.venky.validationservice.persistence.service.ProviderValidationEventPersistenceService;

@Service
public class EventFailureService {
	
	private final ProviderValidationEventPersistenceService eventPersistenceService;
	 private final RetryPolicyResolver retryPolicyResolver;
	
	 public EventFailureService(ProviderValidationEventPersistenceService eventPersistenceService, RetryPolicyResolver retryPolicyResolver) {
		super();
		this.eventPersistenceService = eventPersistenceService;
		this.retryPolicyResolver = retryPolicyResolver;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleFailure(ProviderValidationEventEntity event, Exception ex) {

		int retry = event.getRetryCount() + 1;
		event.setRetryCount(retry);
		event.setLastError(safeErrorMessage(ex.getMessage()));

		RetryPolicy policy = retryPolicyResolver.resolve(event.getEventType());

		if (retry >= policy.getMaxRetry()) {
			event.markDeadLetter();
		} else {
			event.markPending();
			event.setNextRetryAt(Instant.now().plus(policy.getRetryDelay()));
		}

		eventPersistenceService.save(event);
	}

	 @Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markDeadLetter(ProviderValidationEventEntity event, String reason) {
		event.setLastError(safeErrorMessage(reason));
		event.markDeadLetter();
		eventPersistenceService.save(event);
	}

	 @Transactional(propagation = Propagation.REQUIRES_NEW)
	public void scheduleRetry(ProviderValidationEventEntity event, String reason) {
		 RetryPolicy policy = retryPolicyResolver.resolve(event.getEventType());
		 
		  event.setRetryCount(event.getRetryCount() + 1);
	      event.setLastError(safeErrorMessage(reason));
	      event.setNextRetryAt(Instant.now().plus(policy.getRetryDelay()));
	      event.markRetry();
	      eventPersistenceService.save(event);
	}
	 
	 private String safeErrorMessage(String message) {
		    if (message == null) return "UNKNOWN_ERROR";
		    return message.length() > 250
		            ? message.substring(0, 250)
		            : message;
		}

}
