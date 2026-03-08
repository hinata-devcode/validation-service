package com.venky.validationservice.persistence.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.venky.validationservice.application.worker.ProviderResult;
import com.venky.validationservice.persistence.entity.ProviderValidationResultEntity;
import com.venky.validationservice.persistence.repository.ProviderValidationResultRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProviderValidationResultService {

    private final ProviderValidationResultRepository repo;
    

    public ProviderValidationResultService(ProviderValidationResultRepository repo) {
		super();
		this.repo = repo;
	}

	public void store(UUID validationRequestId, String provider, String providerReferenceId,
			ProviderResult result) {
		ProviderValidationResultEntity entity = ProviderValidationResultEntity.from(validationRequestId, provider,
				providerReferenceId, result);
		
		repo.save(entity);
	}
	
	public Optional<ProviderValidationResultEntity> findProviderDetailsById(UUID validationRequestId) {
		return repo.findById(validationRequestId);
	}
}

