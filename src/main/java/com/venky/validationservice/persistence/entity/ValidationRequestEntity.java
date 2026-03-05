package com.venky.validationservice.persistence.entity;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Type;

import com.venky.validationservice.domain.model.ConfidenceLevel;
import com.venky.validationservice.domain.model.ValidationStatus;
import com.venky.validationservice.exception.FailureOrigin;
import com.venky.validationservice.exception.NonRetryableProviderException;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.service.UUIDCharConverter;

@Entity
@Table(name = "validation_request")
public class ValidationRequestEntity {

	@Id
	@Column(name = "validation_request_id", columnDefinition = "CHAR(36)",nullable = false,updatable =false)
	private UUID validationRequestId;
	
	 @Column(name = "idempotency_key", unique = true, nullable = false, length = 36)
	private String idempotencyKey;
	 
	@Column(name="payload_hash",length=64,nullable=false,updatable=false)
	private String payloadHash;

	@Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider;

    @Column(name = "provider_reference_id")
    private String providerReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false)
    private ExecutionStatus executionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status")
    private ValidationStatus validationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level")
    private ConfidenceLevel confidenceLevel;

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
    
    @Column(name="failure_origin")
    @Enumerated(EnumType.STRING)
    private FailureOrigin failureOrigin;

    @Column(name="failure_reason")
	private String failureCode;
    
    @Column(name = "provider_call_initiated_at")
    private Instant providerCallInitiatedAt;

    protected ValidationRequestEntity() {}

    public ValidationRequestEntity(UUID id, ExecutionStatus executionStatus, String idempotency, String incomingHash,Provider provider) {
        this.validationRequestId=id;
        this.executionStatus = executionStatus;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.lastStatusCheckAt=Instant.now();
        this.idempotencyKey=idempotency;
        this.payloadHash=incomingHash;
        this.provider=provider;
    }

	public void setProvider(Provider provider) {
		this.provider=provider;
	}

	public void setProviderReferenceId(String providerReferenceId) {
		this.providerReferenceId=providerReferenceId;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus=executionStatus;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt=updatedAt;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}
	
	public void complete(ValidationStatus validationStatus, ConfidenceLevel confidenceLevel) {

		if (this.executionStatus != ExecutionStatus.PROCESSING) {
		    throw new NonRetryableProviderException(
		        "Validation already processed: " + this.executionStatus,provider
		    );
		}

		this.executionStatus = ExecutionStatus.COMPLETED;
		this.validationStatus = validationStatus;
		this.confidenceLevel = confidenceLevel;
		this.updatedAt = Instant.now();
	}

	public Provider getProvider() {
		return provider;
	}

	public String getProviderReferenceId() {
		return providerReferenceId;
	}

	public ValidationStatus getDecisionStatus() {
		return validationStatus;
	}

	public ConfidenceLevel getConfidenceLevel() {
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

	public void markProviderFailed(String message) {
		this.executionStatus=ExecutionStatus.FAILED;
		 this.setFailureOrigin(FailureOrigin.EXTERNAL_PROVIDER);
		 this.failureCode=message;
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
	    return ExecutionStatus.COMPLETED == this.executionStatus
	            || ExecutionStatus.FAILED == (this.executionStatus);
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

	public ValidationStatus getValidationStatus() {
		return this.validationStatus;
	}

	public UUID getValidationRequestId() {
		return this.validationRequestId;
	}

	public void markValidationFailure(String message) {
	    this.executionStatus = ExecutionStatus.FAILED;
	    this.setFailureOrigin(FailureOrigin.INTERNAL_SYSTEM);
	    this.failureCode=message;
	}

	public FailureOrigin getFailureOrigin() {
		return failureOrigin;
	}

	public void setFailureOrigin(FailureOrigin failureOrigin) {
		this.failureOrigin = failureOrigin;
	}

	public Instant getProviderCallInitiatedAt() {
		return providerCallInitiatedAt;
	}

	public void setProviderCallInitiatedAt(Instant providerCallInitiatedAt) {
		this.providerCallInitiatedAt = providerCallInitiatedAt;
	}
	
	 public int getPollAttempts() {
			return pollAttempts;
		}

	 public String getIdempotencyKey() {
		return idempotencyKey;
	 }

	 public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	 }

	 public String getPayloadHash() {
		return payloadHash;
	 }

	 public void setPayloadHash(String payloadHash) {
		this.payloadHash = payloadHash;
	 }


}

