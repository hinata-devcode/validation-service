package com.venky.validationservice.application.worker;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.repository.ValidationRequestRepository;

public class RecoveryScheduler {
	
	private final ValidationRequestRepository validationRequestRepository;
	
	public RecoveryScheduler(ValidationRequestRepository validationRequestRepository) {
		super();
		this.validationRequestRepository = validationRequestRepository;
	}

	@Scheduled(fixedDelay = 60000)
	public void recoverStuckRequests() {

		List<ValidationRequestEntity> stuck = validationRequestRepository
				.findStuckProcessing(LocalDateTime.now().minusMinutes(2));

		for (ValidationRequestEntity vr : stuck) {

			int updated = validationRequestRepository.markProcessingIfInitiated(vr.getValidationRequestId());

			if (updated == 0) {
				continue;
			}
			// need to implement this later by checking if 3rd party provides
			// Idempotency/reference ID

//			try {
//
//				//ProviderResponse response = providerClient.call(vr.toCommand());
//
//				vr.markSuccess(response.getReferenceId());
//
//			} catch (Exception ex) {
//
//				vr.incrementRetry();
//
//				if (vr.getRetryCount() > 3) {
//					vr.markFailed("Retries exhausted");
//				}
//			}

			validationRequestRepository.save(vr);
		}
	}


}
