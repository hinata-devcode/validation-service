package com.venky.validationservice.domain.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.venky.validationservice.persistence.entity.UserEntity;
import com.venky.validationservice.persistence.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch from DB
    	String normalizedUserName = username.trim().toLowerCase();
        UserEntity userEntity = userRepository.findByUsername(normalizedUserName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Translate roles for Spring Security
        Set<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        // Return Spring's predefined User object
        return new User(userEntity.getUsername(), userEntity.getPassword(), authorities);
    }
}
