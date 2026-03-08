package com.venky.validationservice.persistence.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.venky.validationservice.persistence.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    // Spring Data JPA magically implements this SQL: 
    // SELECT * FROM users WHERE username = ?
    Optional<UserEntity> findByUsername(String username);
}
