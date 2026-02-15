package com.venky.validationservice.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ProviderEventType;

@Entity
@Table(name = "provider_validation_event")
public class ProviderValidationEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // internal DB PK (NOT fav_id)

    @Column(name = "validation_request_id")
    private UUID validationRequestId; // nullable (webhook-before-API case)

    @Enumerated(EnumType.STRING)
    @Column(name = "provider",nullable = false)
    private Provider provider;

    @Column(name = "provider_reference_id", nullable = false)
    private String providerReferenceId; // fav_xxx / stripe_xxx

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private ProviderEventType eventType; // API_RESPONSE / WEBHOOK

    
	@Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload; // store full payload safely

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private EventProcessingStatus status=EventProcessingStatus.PENDING;
    
    @Column(name="retry_count")
    private int retryCount;

    @Column(name="last_error")
    private String lastError;
    
    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ProviderValidationEventEntity() {}

    public ProviderValidationEventEntity(
            UUID validationRequestId,
            Provider provider,
            String providerReferenceId,
            ProviderEventType eventType,
            String rawPayload
    ) {
        this.validationRequestId = validationRequestId;
        this.provider = provider;
        this.providerReferenceId = providerReferenceId;
        this.eventType = eventType;
        this.rawPayload = rawPayload;
    }


	public void markCompleted() {
		this.status=EventProcessingStatus.COMPLETED;
	}

	public String getProviderReferenceId() {
		return providerReferenceId;
	}

	public String getRawPayload() {
		return rawPayload;
	}
	
	public ProviderEventType getEventType() {
		return eventType;
	}

	public Long getId() {
		return id;
	}

	public UUID getValidationRequestId() {
		return validationRequestId;
	}

	public Provider getProvider() {
		return provider;
	}

	public EventProcessingStatus getStatus() {
		return status;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public Instant getNextRetryAt() {
		return nextRetryAt;
	}

	public void setNextRetryAt(Instant nextRetryAt) {
		this.nextRetryAt = nextRetryAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public void setEventType(ProviderEventType eventType) {
		this.eventType = eventType;
	}
	
	public void markProcessing() {
	    this.status = EventProcessingStatus.PROCESSING;
	}

	public void markFailed() {
	    this.status = EventProcessingStatus.FAILED;
	}

	public void markPending() {
	    this.status = EventProcessingStatus.PENDING;
	}

	public void markSkipped() {
	    this.status = EventProcessingStatus.SKIPPED;
	}

	public void setRetryCount(int retry) {
		this.retryCount=retry;
	}


}
