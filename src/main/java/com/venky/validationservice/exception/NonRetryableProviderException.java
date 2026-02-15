package com.venky.validationservice.exception;

public class NonRetryableProviderException extends RuntimeException {

	public NonRetryableProviderException(String message, Throwable ex) {
		super(message,ex);
	}

}
