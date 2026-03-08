package com.venky.validationservice.application.worker;

import org.springframework.stereotype.Component;

import com.venky.validationservice.application.RetryPolicy;

import java.time.Duration;

@Component
public class WebhookRetryPolicy implements RetryPolicy {

    @Override
    public int getMaxRetry() {
        return 5;
    }

    @Override
    public Duration getRetryDelay() {
        return Duration.ofSeconds(60);
    }
} 
