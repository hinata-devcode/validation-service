package com.venky.validationservice.integration.utils;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

@Component
public class RetryExecutor {

	public <T> T execute(Supplier<T> action, int maxAttempts, Duration initialDelay) {

		int attempt = 0;
		Duration delay = initialDelay;

		while (true) {

			try {
				return action.get();
			} catch (Exception ex) {

				attempt++;

				if (attempt >= maxAttempts) {
					throw ex;
				}

				try {
					Thread.sleep(delay.toMillis());
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(ie);
				}

				delay = delay.multipliedBy(2); 
			}
		}
	}
}
