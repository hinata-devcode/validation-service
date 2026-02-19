package com.venky.validationservice.exception;

public class ThirdpartyProviderException extends RuntimeException {

    private final FailureOrigin failureOrigin;

    public ThirdpartyProviderException(String message, Throwable cause) {
        super(message, cause);
        this.failureOrigin = FailureOrigin.EXTERNAL_PROVIDER;
    }

	public ThirdpartyProviderException(String message, FailureOrigin failureOrigin) {
		super(message);
		this.failureOrigin=failureOrigin;
	}

	public FailureOrigin getFailureOrigin() {
        return failureOrigin;
    }
}

//The serializable class RzpException does not declare a static final serialVersionUID field of type long

