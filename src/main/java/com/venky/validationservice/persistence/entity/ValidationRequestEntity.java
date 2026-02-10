package com.venky.validationservice.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

import com.venky.validationservice.integration.common.ExecutionStatus;

@Entity
@Table(name = "validation_request")
public class ValidationRequestEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_reference_id")
    private String providerReferenceId;

    @Column(name = "execution_status", nullable = false)
    private String executionStatus;

    @Column(name = "decision_status")
    private String decisionStatus;

    @Column(name = "confidence_level")
    private String confidenceLevel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ValidationRequestEntity() {}

    public ValidationRequestEntity(UUID id, String executionStatus) {
        this.id = id;
        this.executionStatus = executionStatus;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

	public void setProvider(String provider) {
		this.provider=provider;
	}

	public void setProviderReferenceId(String providerReferenceId) {
		this.providerReferenceId=providerReferenceId;
	}

	public void setExecutionStatus(String executionStatus) {
		this.executionStatus=executionStatus;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt=updatedAt;
	}

	public String getExecutionStatus() {
		return executionStatus;
	}
	
	public void complete(
	        String decisionStatus,
	        String confidenceLevel) {

	    if (!ExecutionStatus.PENDING.name().equals(this.executionStatus)) {
	        return; // idempotency guard
	    }

	    this.executionStatus = ExecutionStatus.COMPLETED.name();
	    this.decisionStatus = decisionStatus;
	    this.confidenceLevel = confidenceLevel;
	    this.updatedAt = Instant.now();
	}


    // getters & setters
}

