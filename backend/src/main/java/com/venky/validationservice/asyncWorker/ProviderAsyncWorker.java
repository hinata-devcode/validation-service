package com.venky.validationservice.asyncWorker;

import java.time.Instant;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.service.ProviderValidationPort;
import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.exception.ProviderCallTimeoutException;
import com.venky.validationservice.exception.ThirdpartyProviderException;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ValidationState;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderAsyncWorker {

    private final ProviderValidationPort providerPort;
    private final ValidationPersistenceService persistenceService;
    
    public Provider getActiveProvider() {
        return providerPort.getProviderName(); 
    }

    @Async("providerTaskExecutor") // Points exactly to the Bean we created in AsyncConfig
	public void executeProviderValidation(FundAccountDetails details, ValidationState validationState) {

		UUID requestId = validationState.getValidationRequestId();

		try {
			Instant callInitiatedAt = Instant.now();

			// ATOMIC UPDATE IN CASE OF MUTIPLE INSTACNES OR TWO THREADS TRYING TO UPDATE
			// SAME REQUEST & UPDATING PROVIDER CALL INITIATED AT
			int updated = persistenceService.markInProcessingIfInitiated(validationState.getValidationRequestId(),
					callInitiatedAt);

			if (updated == 0) {
				log.warn("Concurrent update attempt blocked for validationRequestId: {}",
						validationState.getValidationRequestId());
				return;
			}

			log.info("validationRequestId : {}  moved from INITIATED → PROCESSING",
					validationState.getValidationRequestId());

			log.info("Background Worker starting provider call for validationRequestId: {}", requestId);

			// 1. The heavy lifting (This blocks this specific background thread, but NOT
			// Tomcat)
			providerPort.validate(details, validationState);

			log.info("Background Worker completed provider call successfully for validationRequestId: {}", requestId);

			// Note: Assuming providerPort.validate() handles updating the DB to COMPLETED
			// internally upon success.

		} catch (ProviderCallTimeoutException ex) {
			log.warn("Provider call timed out for validationRequestId: {}, Provider: {}", requestId, ex.getProvider(),
					ex.getMessage());

			persistenceService.markProviderCallTimeout(requestId, ex.getProvider());
		} catch (ThirdpartyProviderException ex) {
			log.error("Provider rejected validationRequestId: {}. Provider: {}", requestId, ex.getProvider(),
					ex.getMessage());

			persistenceService.markValidationRequestFailed(requestId, FailureOrigin.EXTERNAL_PROVIDER, "PROVIDER_ERROR",
					ex.getProvider());

		} catch (RuntimeException ex) {
			log.error("Internal validation error during async processing for validationRequestId: {}", requestId,
					ex.getMessage());
			persistenceService.markValidationRequestFailed(requestId, FailureOrigin.INTERNAL_SYSTEM, "INTERNAL_ERROR",
					validationState.getProvider());
		}
	}
}
