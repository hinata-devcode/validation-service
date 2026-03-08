package com.venky.validationservice.exception;

import com.venky.validationservice.integration.common.Provider;

public class NonRetryableProviderException extends ThirdpartyProviderException {

	public NonRetryableProviderException(String message, Throwable ex,Provider provider) {
		super(message,ex,provider);
	}

	public NonRetryableProviderException(String message,Provider provider) {
		super(message,FailureOrigin.EXTERNAL_PROVIDER,provider);
	}

}
