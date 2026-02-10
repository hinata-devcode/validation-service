package com.venky.validationservice.persistence.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

import com.venky.validationservice.persistence.entity.ValidationRequestEntity;

public interface ValidationRequestRepository
        extends JpaRepository<ValidationRequestEntity, UUID> {

    Optional<ValidationRequestEntity> findByProviderReferenceId(UUID providerReferenceId);
}

