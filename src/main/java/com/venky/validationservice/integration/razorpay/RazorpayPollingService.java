package com.venky.validationservice.integration.razorpay;

import org.springframework.stereotype.Service;

import com.venky.validationservice.application.polling.ProviderPollingService;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ProviderEventType;
import com.venky.validationservice.persistence.entity.EventProcessingStatus;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;
import com.venky.validationservice.persistence.service.ProviderValidationEventPersistenceService;

@Service
public class RazorpayPollingService implements ProviderPollingService {

    private final RazorpayClient razorpayClient;
    private final ProviderValidationEventPersistenceService eventPersistence;

    
    public RazorpayPollingService(RazorpayClient razorpayClient,
			ProviderValidationEventPersistenceService eventPersistence) {
		super();
		this.razorpayClient = razorpayClient;
		this.eventPersistence = eventPersistence;
	}

	@Override
    public Provider getProvider() {
        return Provider.RAZORPAY;
    }

    @Override
    public void poll(ValidationRequestEntity request) {

        String apiResponse =
                razorpayClient.fetchStatus(
                        request.getProviderReferenceId()
                );

        ProviderValidationEventEntity event =
                new ProviderValidationEventEntity(request.getId(),Provider.RAZORPAY,request.getProviderReferenceId(),ProviderEventType.API_RESPONSE,apiResponse);


        event.markPending();
        event.setRetryCount(0);
        eventPersistence.save(event);
    }

}

