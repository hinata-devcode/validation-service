package com.venky.validationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                            "Validation service is temporarily unavailable"
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Something went wrong "+ex.getMessage()
                ));
    }
}

