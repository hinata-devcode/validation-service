package com.venky.validationservice.exception;

public class ProviderCallTimeoutException extends ThirdpartyProviderException {
	public ProviderCallTimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}
}