package com.venky.validationservice.domain.service;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.venky.validationservice.controller.dto.SignupRequestDTO;
import com.venky.validationservice.persistence.entity.UserEntity;
import com.venky.validationservice.persistence.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // This handles the SIGNUP process
    public void registerUser(SignupRequestDTO request) {
    	
    	String userName=request.getUsername().trim().toLowerCase();
        
        // 1. Check if username exists
        if (userRepository.findByUsername(userName).isPresent()) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        // 2. Hash the plain-text password from the UI
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Create the entity and assign the default USER role
        UserEntity newUser = new UserEntity();
        newUser.setUsername(userName);
        newUser.setPassword(hashedPassword); 
        newUser.setRoles(Set.of(UserEntity.Role.USER));

        // 4. Save to DB
        userRepository.save(newUser);
        log.info("Successfully registered new user: {}", request.getUsername());
    }
}