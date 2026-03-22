package com.venky.validationservice.persistence.repository;


import org.springframework.data.domain.Pageable;
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

import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
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
		      AND v.providerReferenceId IS NOT NULL
		      AND (v.lastStatusCheckAt IS NULL 
		           OR v.lastStatusCheckAt <= :threshold)
		""")
		List<ValidationRequestEntity> findRequestsForPolling(
		        @Param("status") ExecutionStatus processing,
		        @Param("threshold") Instant threshold
		);
	
	
	@Transactional
	@Modifying
	@Query("""
	UPDATE ValidationRequestEntity v
	SET v.executionStatus = 'PROCESSING',
	    v.providerCallInitiatedAt= :now
	WHERE v.validationRequestId = :id
	  AND v.executionStatus = 'INITIATED'
	""")
	int markInProcessingIfInitiated(@Param("id") UUID id, @Param("now") Instant now);
		
		@Query("""
				   SELECT e.validationRequestId, COUNT(e)
				   FROM ProviderValidationEventEntity e
				   WHERE e.validationRequestId IN :ids
				     AND e.status IN ('PENDING', 'PROCESSING')
				   GROUP BY e.validationRequestId
				""")
		List<Object[]> countPendingEvents(List<UUID> ids);

		@Query("""
			    SELECT v FROM ValidationRequestEntity v 
			    WHERE v.executionStatus IN ('PROVIDER_CALL_TIMEOUT', 'PROCESSING') 
			    AND v.providerReferenceId IS NULL 
			    AND v.providerCallInitiatedAt IS NOT NULL
			    AND v.pollAttempts < :maxAttempts 
			    AND (v.lastStatusCheckAt IS NULL OR v.lastStatusCheckAt <= :threshold)
			    ORDER BY v.providerCallInitiatedAt ASC
			""")
			List<ValidationRequestEntity> findStuckCases(
			    @Param("threshold") Instant threshold, 
			    @Param("maxAttempts") int maxAttempts,
			    Pageable pageable 
			);
		
		@Modifying
	    @Query("""
	        UPDATE ValidationRequestEntity v 
	        SET v.executionStatus = :status,
	            v.provider = :provider
	        WHERE v.validationRequestId = :id
	    """)
	    int updateExecutionStatus(@Param("id") UUID id, @Param("status") ExecutionStatus status, @Param("provider")  Provider provider);

		
		 Optional<ValidationRequestEntity> findByIdempotencyKey(String idempotencyKey);
	}
