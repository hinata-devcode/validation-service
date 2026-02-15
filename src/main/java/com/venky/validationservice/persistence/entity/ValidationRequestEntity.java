package com.venky.validationservice.persistence.entity;

import jakarta.persistence.*;

import java.time.Duration;
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
    
    @Column(name = "last_status_checkAt")
    private Instant lastStatusCheckAt;

 
    @Column(name = "poll_attempts")
    private int pollAttempts;

    @Column(name = "first_created_at")
    private Instant firstCreatedAt;

    @Column(name="failure_reason")
	private String failureReason;

    protected ValidationRequestEntity() {}

    public ValidationRequestEntity(UUID id, String executionStatus) {
        this.id=id;
        this.executionStatus = executionStatus;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.lastStatusCheckAt=Instant.now();
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

	public UUID getId() {
		return id;
	}

	public String getProvider() {
		return provider;
	}

	public String getProviderReferenceId() {
		return providerReferenceId;
	}

	public String getDecisionStatus() {
		return decisionStatus;
	}

	public String getConfidenceLevel() {
		return confidenceLevel;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getLastStatusCheckAt() {
		return lastStatusCheckAt;
	}

	public void setLastStatusCheckAt(Instant now) {
		this.lastStatusCheckAt=now;
	}

	public void markProviderFailed() {
		this.executionStatus=ExecutionStatus.PROVIDER_FAILED.name();
	}
	
	
	
	public void incrementPollAttempts() {
	    this.pollAttempts++;
	}

	public void markFirstCreatedIfAbsent() {
	    if (this.firstCreatedAt == null) {
	        this.firstCreatedAt = Instant.now();
	    }
	}

	public void updateLastStatusCheck() {
	    this.lastStatusCheckAt = Instant.now();
	}

	public boolean isTerminal() {
	    return ExecutionStatus.COMPLETED.toString().equals(this.executionStatus)
	            || ExecutionStatus.FAILED.toString().equals(this.executionStatus);
	}

	public boolean isPollingTimedOut(Duration maxDuration, int maxAttempts) {

	    if (pollAttempts >= maxAttempts) {
	        return true;
	    }

	    if (firstCreatedAt == null) {
	        return false;
	    }

	    Duration elapsed = Duration.between(firstCreatedAt, Instant.now());

	    return elapsed.compareTo(maxDuration) > 0;
	}

	public void markProviderTimeoutFailure() {
	    this.executionStatus = ExecutionStatus.FAILED.toString();
	    this.failureReason = "Provider timeout";
	}



    // getters & setters
}

