package com.venky.validationservice.persistence.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.venky.validationservice.persistence.entity.ValidationRequestEntity;

public interface ValidationRequestRepository
        extends JpaRepository<ValidationRequestEntity, UUID> {

//	@Query("""
//		    SELECT v FROM ValidationRequestEntity v
//		    WHERE v.providerReferenceId = :providerReferenceId
//		""")
//	Optional<ValidationRequestEntity> findByProviderReferenceId(@Param("providerReferenceId")String providerReferenceId);
	
	Optional<ValidationRequestEntity> findByProviderReferenceId(String providerReferenceId);
	
	@Query("""
		    SELECT v FROM ValidationRequestEntity v
		    WHERE v.executionStatus = :status
		      AND (v.lastStatusCheckAt IS NULL 
		           OR v.lastStatusCheckAt <= :threshold)
		""")
		List<ValidationRequestEntity> findRequestsForPolling(
		        @Param("status") String status,
		        @Param("threshold") Instant threshold
		);
	
	
	@Transactional
	@Modifying
	@Query("""
	       UPDATE ValidationRequest vr
	       SET vr.status = 'PROCESSING',
	           vr.updatedAt = CURRENT_TIMESTAMP
	       WHERE vr.id = :id
	       AND vr.status = 'INITIATED'
	       """)
	int markProcessingIfInitiated(@Param("id") UUID id);
	
	
	@Query("""
		       SELECT vr
		       FROM ValidationRequest vr
		       WHERE vr.status = 'PROCESSING'
		       AND vr.providerReferenceId IS NULL
		       AND vr.updatedAt < :cutoff
		       """)
		List<ValidationRequestEntity> findStuckProcessing(
		        @Param("cutoff") LocalDateTime cutoff);



}

