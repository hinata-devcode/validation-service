package com.venky.validationservice.application.polling;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

@Service
public class PollingScheduler {

    private static final int POLLING_THRESHOLD_SECONDS = 30;

    private final ValidationPersistenceService validationPersistence;
    private final Map<Provider, ProviderPollingService> pollingServiceMap;

    public PollingScheduler(
            ValidationPersistenceService validationPersistence,
            List<ProviderPollingService> pollingServices
    ) {
        this.validationPersistence = validationPersistence;

        this.pollingServiceMap = pollingServices.stream()
                .collect(Collectors.toMap(
                        ProviderPollingService::getProvider,
                        Function.identity()
                ));
    }

    @Scheduled(fixedDelay = 10000)
    public void triggerPolling() {

        Instant threshold =
                Instant.now().minusSeconds(POLLING_THRESHOLD_SECONDS);

        List<ValidationRequestEntity> stuckRequests =
                validationPersistence.findRequestsForPolling(
                        ExecutionStatus.PENDING.toString(),
                        threshold
                );

        for (ValidationRequestEntity request : stuckRequests) {

            Provider provider =
                    Provider.valueOf(request.getProvider());

            ProviderPollingService pollingService =
                    pollingServiceMap.get(provider);

            if (pollingService != null) {
                pollingService.poll(request);
                request.setLastStatusCheckAt(Instant.now());
                validationPersistence.updateValidationEntity(request);
            }
        }
    }
}

