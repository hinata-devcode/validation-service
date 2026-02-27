package com.venky.validationservice.reconciliation;

import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.repository.ValidationRequestRepository;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        
        List<ValidationRequestEntity> stuckRequests = 
            repository.findStuckCases(threshold, MAX_ATTEMPTS);
            
        if (stuckRequests.isEmpty()) {
            return;
        }

        // Convert List of handlers to a Map for O(1) lookup
        Map<String, ProviderReconciliationHandler> handlerMap = handlers.stream()
            .collect(Collectors.toMap(ProviderReconciliationHandler::getProvider, h -> h));

        // Group the stuck requests by Provider (e.g., all "RAZORPAY" requests together) 
        //using enums so RAZORPAY will be same for ProviderReconciliationHandler & ValidationRequestEntity
        Map<String, List<ValidationRequestEntity>> requestsByProvider = stuckRequests.stream()
            .collect(Collectors.groupingBy(ValidationRequestEntity::getProvider));

        // Pass the batched lists to their respective handlers
        for (Map.Entry<String, List<ValidationRequestEntity>> entry : requestsByProvider.entrySet()) {
            try {
                ProviderReconciliationHandler handler = handlerMap.get(entry.getKey());
                if (handler != null) {
                    handler.reconcile(entry.getValue()); // Pass the entire list
                }
            } catch (Exception ex) {
                // TODO: Add logging here when your logging framework is set up [cite: 85]
                System.err.println("Failed to reconcile for provider: " + entry.getKey());
            }
        }
    }
}
