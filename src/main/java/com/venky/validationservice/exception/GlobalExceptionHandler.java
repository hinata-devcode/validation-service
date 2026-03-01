package com.venky.validationservice.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {

		// Extract all the field errors and combine them into a single message
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.toList());

		String errorMessage = "Request validation failed: " + String.join(", ", errors);

		log.warn("Bad Request received: {}", errorMessage);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse("INVALID_REQUEST_PARAMETERS", errorMessage,"DOES_NOT_EXIST"));
	}
    
    
    
}

