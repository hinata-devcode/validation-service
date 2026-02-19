package com.venky.validationservice.exception;

public class NonRetryableProviderException extends ThirdpartyProviderException {

	public NonRetryableProviderException(String message, Throwable ex) {
		super(message,ex);
	}

	public NonRetryableProviderException(String message) {
		super(message,FailureOrigin.EXTERNAL_PROVIDER);
	}

}
