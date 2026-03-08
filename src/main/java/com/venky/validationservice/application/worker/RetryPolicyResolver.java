package com.venky.validationservice.application.worker;

import org.springframework.stereotype.Component;

import com.venky.validationservice.application.RetryPolicy;
import com.venky.validationservice.integration.common.ProviderEventType;

@Component
public class RetryPolicyResolver {

    private final WebhookRetryPolicy webhookRetryPolicy;
    private final FetchApiRetryPolicy fetchApiRetryPolicy;

    public RetryPolicyResolver(WebhookRetryPolicy webhookRetryPolicy,
                                FetchApiRetryPolicy fetchApiRetryPolicy) {
        this.webhookRetryPolicy = webhookRetryPolicy;
        this.fetchApiRetryPolicy = fetchApiRetryPolicy;
    }

    public RetryPolicy resolve(ProviderEventType eventType) {

        switch (eventType) {
            case WEBHOOK:
                return webhookRetryPolicy;
            case API_RESPONSE:
                return fetchApiRetryPolicy;
            default:
                throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
}
