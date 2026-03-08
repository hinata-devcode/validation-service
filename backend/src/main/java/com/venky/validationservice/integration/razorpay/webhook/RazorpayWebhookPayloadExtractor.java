package com.venky.validationservice.integration.razorpay.webhook;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class RazorpayWebhookPayloadExtractor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractValidationId(String payload) {
    	  try {
    	        JsonNode root = new ObjectMapper().readTree(payload);
    	        return root
    	            .path("payload")
    	            .path("fund_account.validation")
    	            .path("entity")
    	            .path("id")
    	            .asText();
    	    } catch (Exception e) {
    	        throw new IllegalArgumentException("Invalid webhook payload", e);
    	    }
    }
}

