package com.venky.validationservice.exception;

import com.venky.validationservice.integration.common.Provider;

public class ProviderCallTimeoutException extends ThirdpartyProviderException {

	public ProviderCallTimeoutException(String msg, Throwable cause,Provider provider) {
		super(msg, cause,provider);
	}

	
}