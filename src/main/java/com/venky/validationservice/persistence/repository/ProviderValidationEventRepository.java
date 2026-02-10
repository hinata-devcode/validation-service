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

	List<ProviderValidationEventEntity> findByProcessedFalseOrderByCreatedAtAsc();

	@Query("""
			SELECT e FROM ProviderValidationEventEntity e
			WHERE e.processed = false
			ORDER BY e.createdAt
			LIMIT 1
			FOR UPDATE SKIP LOCKED;
			""")
	Optional<ProviderValidationEventEntity> findNextUnprocessed();
}

