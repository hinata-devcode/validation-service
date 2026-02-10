package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayResponse {

    @JsonProperty("id")
    private String validationId;   // fav_xxx

    @JsonProperty("status")
    private String status;          // created

    @JsonProperty("fund_account")
    private FundAccountRef fundAccount;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FundAccountRef {
        @JsonProperty("id")
        private String fundAccountId; // fa_xxx
    }
    
    /**
     * Snapshot used ONLY for event persistence.
     * This is NOT full Razorpay response.
     */
    public String toEventPayload() {
        return String.format(
            "{ \"validation_id\": \"%s\", \"status\": \"%s\", \"fund_account_id\": \"%s\" }",
            validationId,
            status,
            fundAccount != null ? fundAccount.getFundAccountId() : null
        );
    }
}
