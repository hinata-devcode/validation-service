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
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_reference_id", nullable = false)
    private String providerReferenceId; // fav_xxx / stripe_xxx

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private ProviderEventType eventType; // API_RESPONSE / WEBHOOK

    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload; // store full payload safely

    @Column(nullable = false)
    private boolean processed = false;

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


	public void markProcessed() {
		this.processed=true;
	}

	public UUID getProviderReferenceId() {
		return validationRequestId;
	}

	public String getRawPayload() {
		return rawPayload;
	}

    
}
