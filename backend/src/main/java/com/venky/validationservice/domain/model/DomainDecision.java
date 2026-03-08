package com.venky.validationservice.domain.model;

import com.venky.validationservice.integration.razorpay.DecisionStatus;

import lombok.Data;

@Data
public class DomainDecision {

	private DecisionStatus decision;
	private String confidence;

	public DomainDecision(DecisionStatus decision, String confidence) {
		super();
		this.setDecision(decision);
		this.setConfidence(confidence);
	}

}
