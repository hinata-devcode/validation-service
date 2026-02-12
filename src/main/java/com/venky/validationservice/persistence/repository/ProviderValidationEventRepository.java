package com.venky.validationservice.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;

public interface ProviderValidationEventRepository
        extends JpaRepository<ProviderValidationEventEntity, Long> {

    List<ProviderValidationEventEntity> findByProcessedFalse();
    
    Optional<ProviderValidationEventEntity>
    findTopByProviderAndProviderReferenceIdOrderByCreatedAtDesc(
            Provider provider,
            String providerReferenceId
    );

	//List<ProviderValidationEventEntity> findByProcessedFalseOrderByCreatedAtAsc();

    @Query(value = """
    	    SELECT *
    	    FROM provider_validation_event
    	    WHERE status = 'PENDING'
    	      AND (next_retry_at IS NULL OR next_retry_at <= NOW())
    	    ORDER BY created_at
    	    LIMIT 1
    	    FOR UPDATE SKIP LOCKED
    	""", nativeQuery = true)
	Optional<ProviderValidationEventEntity> findNextUnprocessed();
}

