package com.venky.validationservice.persistence.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.venky.validationservice.application.worker.ProviderResult;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "provider_validation_result", uniqueConstraints = {
		@UniqueConstraint(name = "uk_provider_provider_ref", columnNames = { "provider", "provider_reference_id" }) })
public class ProviderValidationResultEntity {

    @Id
    private UUID validationRequestId;

    private String provider;

    private String providerReferenceId;

    private String providerStatus;

    @Lob
    private String sanitizedPayload;

    @ElementCollection
    @CollectionTable(name = "provider_validation_attributes")
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    private Instant receivedAt;

	public static ProviderValidationResultEntity from(
	        UUID requestId,
	        String provider,
	        String providerReferenceId,
	        ProviderResult result
	) {
	    ProviderValidationResultEntity e = new ProviderValidationResultEntity();
	    e.setValidationRequestId(requestId);
	    e.setProvider(provider);
	    e.setProviderReferenceId(providerReferenceId);
	    e.setProviderStatus(result.getProviderStatus());
	    e.setSanitizedPayload(result.getSanitizedRawPayload());
	    e.setAttributes(result.getAttributes());
	    e.setReceivedAt(Instant.now());
	    return e;
	}

    
}
