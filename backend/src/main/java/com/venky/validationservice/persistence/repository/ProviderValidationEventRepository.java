package com.venky.validationservice.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.EventExecutionStatus;
import com.venky.validationservice.persistence.entity.ProviderValidationEventEntity;

import jakarta.persistence.LockModeType;


public interface ProviderValidationEventRepository
		extends JpaRepository<ProviderValidationEventEntity, Long> {

	// pessimistic lock is if tomorrow mutiple instances picks same row to update
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
	    SELECT e
	    FROM ProviderValidationEventEntity e
	    WHERE e.status IN (
	        com.venky.validationservice.persistence.entity.EventExecutionStatus.PENDING,
	        com.venky.validationservice.persistence.entity.EventExecutionStatus.RETRY_SCHEDULED
	    )
	    AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
	    ORDER BY e.createdAt
	""")
	List<ProviderValidationEventEntity> findNextProcessableEvents(@Param("now") Instant now,Pageable pageable);
	
	
//	@Query(value = "SELECT * FROM provider_validation_event " + "WHERE status = 'PENDING' "
//			+ "AND (next_retry_at IS NULL OR next_retry_at <= NOW()) " + "ORDER BY created_at "
//			+ "LIMIT ?1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
//	List<ProviderValidationEventEntity> fetchBatch(int limit);
}

