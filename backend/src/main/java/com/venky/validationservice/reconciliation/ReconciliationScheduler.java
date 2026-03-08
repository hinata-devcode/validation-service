package com.venky.validationservice.reconciliation;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.repository.ValidationRequestRepository;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReconciliationScheduler {
    
    private final ValidationRequestRepository repository;
    private final List<ProviderReconciliationHandler> handlers;
    
    private static final int MAX_ATTEMPTS = 5;
    private static final long DELAY_SECONDS = 60;

    public ReconciliationScheduler(ValidationRequestRepository repository, 
                                   List<ProviderReconciliationHandler> handlers) {
        this.repository = repository;
        this.handlers = handlers; 
    }

    @Scheduled(fixedDelay = 60000)
    public void reconcileTimedOutRequests() {
        Instant threshold = Instant.now().minusSeconds(DELAY_SECONDS);
        
        List<ValidationRequestEntity> timedOutRequests = 
            repository.findStuckCases(threshold, MAX_ATTEMPTS,PageRequest.of(0, 100));
            
        if (timedOutRequests.isEmpty()) {
        	log.debug("ReconciliationScheduler ran. No timed-out requests found.");
            return;
        }
        
        log.info("ReconciliationScheduler woke up. Found {} requests in PROVIDER_CALL_TIMEOUT state.", timedOutRequests.size());

        // Convert List of handlers to a Map for O(1) lookup
        Map<Provider, ProviderReconciliationHandler> handlerMap = handlers.stream()
            .collect(Collectors.toMap(ProviderReconciliationHandler::getProvider, h -> h));

        // Group the stuck requests by Provider (e.g., all "RAZORPAY" requests together) 
        //using enums so RAZORPAY will be same for ProviderReconciliationHandler & ValidationRequestEntity
        Map<Provider, List<ValidationRequestEntity>> requestsByProvider = timedOutRequests.stream()
            .collect(Collectors.groupingBy(ValidationRequestEntity::getProvider));

        // Pass the batched lists to their respective handlers
        for (Map.Entry<Provider, List<ValidationRequestEntity>> entry : requestsByProvider.entrySet()) {
            try {
                ProviderReconciliationHandler handler = handlerMap.get(entry.getKey());
                if (handler != null) {
                    handler.reconcile(entry.getValue()); // Pass the entire list
                }
            } catch (Exception ex) {
                // TODO: Add logging here when your logging framework is set up [cite: 85]
                log.error("Failed to reconcile for provider: " + entry.getKey());
            }
        }
    }
}
