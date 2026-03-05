package com.venky.validationservice.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationExecutionException.class)
    public ResponseEntity<ErrorResponse> handleValidationExecutionException(
            ValidationExecutionException ex) {

        if (ex.getFailureOrigin() == FailureOrigin.EXTERNAL_PROVIDER) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse(
                            "VALIDATION_TEMPORARILY_UNAVAILABLE",
                            "Validation service is temporarily unavailable due to Provider Issue",
                            ex.getValidationRequestId().toString()
                    ));
        }
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Something went wrong in internal application "+ex.getMessage(),
                        ex.getValidationRequestId().toString()
                ));
    }
    //for Dtos Eg:BankRequestDTo
    @ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {

		// Extract all the field errors and combine them into a single message
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.toList());

		String errorMessage = "Request validation failed: " + String.join(", ", errors);

		log.warn("Bad Request received: {}", errorMessage);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse("INVALID_REQUEST_PARAMETERS", errorMessage,"N/A"));
	}
    
    //for simple method params Eg: String,int, second this is checked
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolations(ConstraintViolationException ex) {
        
        // Extract the error messages (e.g., "Idempotency-Key must not exceed 36 characters")
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_REQUEST_PARAMETERS", errorMessage, "N/A"));
    }
    
    //when headers are missing first this is checked
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        
        // Spring is smart enough to tell us exactly WHICH header is missing!
        String missingHeader = ex.getHeaderName();
        String errorMessage = "Required HTTP header is missing: " + missingHeader;
        
        log.warn("Bad Request: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "MISSING_HEADER", 
                        errorMessage, 
                        "N/A" 
                ));
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Failed login attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        "UNAUTHORIZED", 
                        "Invalid username or password.", 
                        "N/A" // No validation request ID for auth errors
                ));
    }

    /**
     * Handles "Username already taken" during Signup
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument provided: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "BAD_REQUEST", 
                        ex.getMessage(), 
                        "N/A"
                ));
    }

    /**
     * Handles @PreAuthorize("hasRole('ADMIN')") failures
     * Returns 403 FORBIDDEN
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        "ACCESS_DENIED", 
                        "You do not have permission to access this resource.", 
                        "N/A"
                ));
    }
    
    
    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflict(IdempotencyConflictException ex) {
        log.warn("Idempotency Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "IDEMPOTENCY_CONFLICT", 
                        ex.getMessage(), 
                        "N/A" // Or pass the UUID here if you want to extract it
                ));
    }
    
    
}

