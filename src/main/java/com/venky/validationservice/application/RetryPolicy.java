package com.venky.validationservice.application;

import java.time.Duration;

public interface RetryPolicy {

	int getMaxRetry();

	Duration getRetryDelay();

}
