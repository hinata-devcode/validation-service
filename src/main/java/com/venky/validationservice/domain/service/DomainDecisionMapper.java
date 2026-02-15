package com.venky.validationservice.domain.service;

import org.springframework.stereotype.Component;

import com.venky.validationservice.domain.model.ConfidenceLevel;
import com.venky.validationservice.domain.model.ValidationStatus;
import com.venky.validationservice.integration.razorpay.DecisionStatus;

@Component
public class DomainDecisionMapper {

    public ValidationStatus mapValidationStatus(DecisionStatus decisionStatus) {

        if (decisionStatus == null) {
            return ValidationStatus.UNKNOWN;
        }

        switch (decisionStatus) {
            case VALID:
                return ValidationStatus.VALID;

            case INVALID:
                return ValidationStatus.INVALID;

            case UNKNOWN:
            default:
                return ValidationStatus.UNKNOWN;
        }
    }

    public ConfidenceLevel mapConfidenceLevel(String providerConfidenceScore) {

        if (providerConfidenceScore == null) {
            return ConfidenceLevel.LOW;
        }

        double score;

        try {
            score = Double.parseDouble(providerConfidenceScore);
        } catch (NumberFormatException e) {
            return ConfidenceLevel.LOW;
        }

        if (score >= 85) {
            return ConfidenceLevel.HIGH;
        } else if (score >= 60) {
            return ConfidenceLevel.MEDIUM;
        } else {
            return ConfidenceLevel.LOW;
        }
    }
}

