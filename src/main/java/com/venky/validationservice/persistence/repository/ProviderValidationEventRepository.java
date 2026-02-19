package com.venky.validationservice.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.EventProcessingStatus;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;

import jakarta.persistence.LockModeType;

public interface ProviderValidationEventRepository
		extends JpaRepository<ProviderValidationEventEntity, Long> {

	// pessimistic lock is if tomorrow mutiple instances picks same row to update
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(value = """
				     SELECT e
			FROM ProviderValidationEventEntity e
			WHERE e.status = :status
			  AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= CURRENT_TIMESTAMP)
			ORDER BY e.createdAt
				""")
	List<ProviderValidationEventEntity> findNextUnprocessed(@Param("status") EventProcessingStatus status,
			Pageable pageable);
}

