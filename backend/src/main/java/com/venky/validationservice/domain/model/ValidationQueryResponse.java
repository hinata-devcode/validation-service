package com.venky.validationservice.domain.model;

import java.util.UUID;

import com.venky.validationservice.integration.common.ExecutionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidationQueryResponse {

    private UUID validationRequestId;
    private ExecutionStatus executionStatus;
    private ValidationResult result; // null if not completed
}

