package com.venky.validationservice.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.venky.validationservice.controller.dto.LoginRequestDTO;
import com.venky.validationservice.controller.dto.SignupRequestDTO;
import com.venky.validationservice.domain.service.AuthService;
import com.venky.validationservice.security.JwtProperties;
import com.venky.validationservice.security.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final JwtProperties jwtProperties;
    
    @PostMapping("/signup")
	public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDTO request) {
		authService.registerUser(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("message", "User registered successfully. Please login."));

	}

    @PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request, HttpServletResponse response) {

		// 1. Let the Boss handle the heavy lifting
		Authentication verifiedBadge = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		// 2. Extract the authenticated user info
		UserDetails userDetails = (UserDetails) verifiedBadge.getPrincipal();

		// 3. Create the JWT
		String jwtToken = jwtUtil.generateToken(userDetails);

		// 4. Securely attach it to the browser's "tray"
		Cookie cookie = new Cookie("access_token", jwtToken);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setSecure(jwtProperties.isCookieSecure());
		cookie.setMaxAge((int) (jwtProperties.getExpirationMs() / 1000));
		response.addCookie(cookie);

		return ResponseEntity.ok(Map.of("message", "Login successful"));

	}
    
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // To "delete" a cookie, we send a new cookie with the SAME name, 
        // but we set its life (Max-Age) to ZERO seconds.
        Cookie cookie = new Cookie("access_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(jwtProperties.isCookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge(0); // <--- THIS IS THE "KILL" COMMAND
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}

