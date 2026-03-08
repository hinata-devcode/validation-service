package com.venky.validationservice.domain.service;

import org.springframework.stereotype.Service;

import com.venky.validationservice.application.worker.ProviderResult;
import com.venky.validationservice.domain.model.DomainDecision;
import com.venky.validationservice.integration.razorpay.DecisionStatus;

@Service
public class ValidationDecisionService {

	public DomainDecision decide(ProviderResult result) {

	    Boolean accountActive = result.getAccountActive();
	    String nameMatchScore = result.getNameMatchScore();

	    DecisionStatus decision =
	            Boolean.TRUE.equals(accountActive)
	                    ? DecisionStatus.VALID
	                    : DecisionStatus.INVALID;

	    String confidence =
	            nameMatchScore != null
	                    ? String.valueOf(nameMatchScore)
	                    : nameMatchScore;

	    return new DomainDecision(decision, confidence);
	}

}
