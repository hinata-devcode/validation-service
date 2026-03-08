package com.venky.validationservice.integration.razorpay;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayValidationItem {

    private String id;
    private String status;

    @JsonProperty("reference_id")
    private String referenceId;
    
    private Map<String, String> notes;
}
