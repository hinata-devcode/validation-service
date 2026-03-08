package com.venky.validationservice.application;

import java.util.UUID;

import com.venky.validationservice.domain.model.ValidationStatus;
import com.venky.validationservice.integration.common.ExecutionStatus;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class ValidationResponseDTO {
	private UUID validationRequestId; 
    private ExecutionStatus executionStatus;
    private String message;
}

