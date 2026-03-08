package com.venky.validationservice.application.worker;

import org.springframework.stereotype.Component;

import com.venky.validationservice.application.RetryPolicy;

import java.time.Duration;

@Component
public class FetchApiRetryPolicy implements RetryPolicy {

    @Override
    public int getMaxRetry() {
        return 1; // Important: only 1
    }

    @Override
    public Duration getRetryDelay() {
        return Duration.ofSeconds(120);
    }
}