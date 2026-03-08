package com.venky.validationservice.integration.utils;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.venky.validationservice.exception.RetryableProviderException;

@Component
public class RetryExecutor {

	public <T> T execute(Supplier<T> action, int maxAttempts, Duration initialDelay) {

		int attempt = 1;
		Duration delay = initialDelay;

		while (true) {

			try {
				return action.get();

			} catch (Exception ex) {

				if (!isRetryable(ex) || attempt >= maxAttempts) {
					throw ex;
				}

				sleep(delay);

				delay = delay.multipliedBy(2);
				attempt++;
			}
		}
	}

	private boolean isRetryable(Exception ex) {
		return ex instanceof RetryableProviderException;
	}

	private void sleep(Duration delay) {
		try {
			Thread.sleep(delay.toMillis());
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Retry interrupted", ie);
		}
	}
}
