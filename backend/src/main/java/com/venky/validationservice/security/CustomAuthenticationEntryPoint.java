package com.venky.validationservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venky.validationservice.exception.ErrorResponse; // Import your actual class!
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper mapper;
	
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("Unauthorized access attempt to URL: {}", request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // 1. Create your ACTUAL ErrorResponse Java Object
        // We pass "N/A" because, as you correctly pointed out, the ID doesn't exist yet!
        ErrorResponse errorBody = new ErrorResponse(
                "UNAUTHORIZED",
                "Authentication is required to access this resource. Please log in.",
                "N/A" 
        );

       
        String jsonString = mapper.writeValueAsString(errorBody);

        // 3. Put it on Tomcat's Tray
        response.getWriter().write(jsonString);
    }
}