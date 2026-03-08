package com.venky.validationservice.application;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UuidValidationRequestIdGenerator
        implements ValidationRequestIdGenerator {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}

