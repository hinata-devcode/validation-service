package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RazorpayExternalRequest {
    @JsonProperty("source_account_number")
    private String sourceAccountNumber;
    
    @JsonProperty("validation_type")
    private String validationType;
    
    @JsonProperty("reference_id")
    private String referenceId;
    
    @JsonProperty("fund_account")
    private FundAccount fundAccount;
   
}