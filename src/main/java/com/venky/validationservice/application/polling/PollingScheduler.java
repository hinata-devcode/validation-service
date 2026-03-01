package com.venky.validationservice.application.polling;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PollingScheduler {

    private static final int POLLING_THRESHOLD_SECONDS = 30;

    private final ValidationPersistenceService validationPersistence;
    private final Map<Provider, ProviderPollingService> pollingServiceMap;

	public PollingScheduler(ValidationPersistenceService validationPersistence,
			List<ProviderPollingService> pollingServices) {
		this.validationPersistence = validationPersistence;

		this.pollingServiceMap = pollingServices.stream()
				.collect(Collectors.toMap(ProviderPollingService::getProvider, Function.identity()));
	}

	@Scheduled(fixedDelay = 5000)
	public void triggerPolling() {

		Instant threshold = Instant.now().minusSeconds(POLLING_THRESHOLD_SECONDS);

		List<ValidationRequestEntity> stuckRequests = validationPersistence
				.findRequestsForPolling(ExecutionStatus.PROCESSING, threshold);

		if (stuckRequests.isEmpty()) {
			 log.debug("PollingScheduler ran. No pending requests found.");
			return;
		}

		log.info("PollingScheduler found {} pending validation requests. Initiating poll.", stuckRequests.size());
		List<UUID> ids = stuckRequests.stream().map(ValidationRequestEntity::getValidationRequestId).toList();

		Map<UUID, Long> pendingCountMap = getPendingCountMap(ids);

		processRequests(stuckRequests, pendingCountMap);
	}

	private void processRequests(List<ValidationRequestEntity> requests, Map<UUID, Long> pendingCountMap) {

		for (ValidationRequestEntity request : requests) {

			Long pendingCount = pendingCountMap.getOrDefault(request.getValidationRequestId(), 0L);

			if (pendingCount >= 1) {
				continue; // Skip — event already pending
			}

			Provider provider =request.getProvider();

			ProviderPollingService pollingService = pollingServiceMap.get(provider);

			if (pollingService != null) {
				log.info("Polling provider for status update. valReqId={}, providerRefId={}",
						request.getValidationRequestId(), request.getProviderReferenceId());
				pollingService.poll(request);
			}
		}
	}

	private Map<UUID, Long> getPendingCountMap(List<UUID> ids) {

		List<Object[]> results = validationPersistence.countPendingEvents(ids);

		Map<UUID, Long> map = new HashMap<>();

		for (Object[] row : results) {
			UUID requestId = (UUID) row[0];
			Long count = (Long) row[1];
			map.put(requestId, count);
		}

		return map;
	}
}

