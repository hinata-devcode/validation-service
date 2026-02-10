package com.venky.validationservice.domain.service;

import org.springframework.stereotype.Service;

import com.venky.validationservice.domain.model.DomainDecision;
import com.venky.validationservice.integration.razorpay.DecisionStatus;
import com.venky.validationservice.integration.razorpay.RazorpayWebhookResult;

@Service
public class ValidationDecisionService {

    public DomainDecision decide(RazorpayWebhookResult result) {

        String accountStatus =
            result.getAttributes().get("account_status");

        String nameMatch =
            result.getAttributes().get("name_match_score");

        DecisionStatus decision =
            "active".equalsIgnoreCase(accountStatus)
                ? DecisionStatus.VALID
                : DecisionStatus.INVALID;

        String confidence =
            nameMatch != null ? nameMatch : "UNKNOWN";

        return new DomainDecision(decision.name(), confidence);
    }
}
