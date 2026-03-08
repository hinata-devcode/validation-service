package com.venky.validationservice.exception;

import com.venky.validationservice.integration.common.Provider;

public class ThirdpartyProviderException extends RuntimeException {

    private final FailureOrigin failureOrigin;
    private final Provider provider;

    public ThirdpartyProviderException(String message, Throwable cause,Provider provider) {
        super(message, cause);
        this.failureOrigin = FailureOrigin.EXTERNAL_PROVIDER;
        this.provider=provider;
    }

	public ThirdpartyProviderException(String message, FailureOrigin failureOrigin,Provider provider) {
		super(message);
		this.failureOrigin=failureOrigin;
		 this.provider=provider;
	}

	public FailureOrigin getFailureOrigin() {
        return failureOrigin;
    }

	public Provider getProvider() {
		return provider;
	}
	
}

//The serializable class RzpException does not declare a static final serialVersionUID field of type long

