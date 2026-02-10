package com.venky.validationservice.application;

import com.venky.validationservice.domain.model.ValidationStatus;
import com.venky.validationservice.integration.common.ValidationExecutionResult;


public class ValidationResponseDTO {

    private final ValidationExecutionResult executionResult;

    public ValidationResponseDTO(ValidationExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public static ValidationResponseDTO from(
            ValidationExecutionResult executionResult) {
        return new ValidationResponseDTO(executionResult);
    }

    public ValidationExecutionResult getExecutionResult() {
        return executionResult;
    }
}

