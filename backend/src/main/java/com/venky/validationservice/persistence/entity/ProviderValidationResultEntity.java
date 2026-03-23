package com.venky.validationservice.persistence.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.venky.validationservice.application.worker.ProviderResult;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    @Column(name = "validation_request_id", columnDefinition = "CHAR(36)")
    private UUID validationRequestId;

    private String provider;

    @Column(name="provider_reference_id")
    private String providerReferenceId;

    @Column(name="provider_status")
    private String providerStatus;
    
    @Column(name = "provider_account_status")
    private String providerAccountStatus;

    @Column(name = "provider_name_match_score")
    private String providerNameMatchScore;

    @Column(name = "provider_registered_name")
    private String providerRegisteredName;

    @Column(name = "provider_account_active")
    private Boolean providerAccountActive;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_bank_details_json", columnDefinition = "json")
    private String bankDetailsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sanitized_payload", columnDefinition = "json")
    private String sanitizedPayload;

    @ElementCollection
    @CollectionTable(
    	    name = "provider_validation_attributes",
    	    joinColumns = @JoinColumn(name = "validation_request_id")
    	)
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metaDataAttributes = new HashMap<>();

    private Instant receivedAt;

	public static ProviderValidationResultEntity from(UUID validationRequestId, String providerName,
			String providerReferenceId, ProviderResult result) {

		ProviderValidationResultEntity entity = new ProviderValidationResultEntity();

		entity.setValidationRequestId(validationRequestId);
		entity.setProvider(providerName);
		entity.setProviderReferenceId(result.getProviderReferenceId());
		entity.setProviderStatus(result.getProviderStatus().name());

		entity.setProviderAccountStatus(result.getProviderAccountStatus());
		entity.setProviderAccountActive(result.getAccountActive());
		entity.setProviderNameMatchScore(result.getNameMatchScore());
		entity.setProviderRegisteredName(result.getRegisteredName());

		entity.setSanitizedPayload(result.getSanitizedRawPayload());
		entity.setMetaDataAttributes(result.getAttributes());
		entity.setBankDetailsJson(result.getBankDetailsJson());
		entity.setReceivedAt(Instant.now());

		return entity;

	}
    
}
