package com.venky.validationservice.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.venky.validationservice.persistence.entity.ProviderValidationResultEntity;

public interface ProviderValidationResultRepository extends JpaRepository<ProviderValidationResultEntity, UUID>{

}
