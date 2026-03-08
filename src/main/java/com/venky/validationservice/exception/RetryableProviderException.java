package com.venky.validationservice.exception;

import com.venky.validationservice.integration.common.Provider;

public class RetryableProviderException extends ThirdpartyProviderException {
    public RetryableProviderException(String msg, Throwable cause,Provider provider) {
        super(msg, cause,provider);
    }
}
