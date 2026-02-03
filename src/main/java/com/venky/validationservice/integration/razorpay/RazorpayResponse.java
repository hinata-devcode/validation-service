package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayResponse {
    private String id; // The "fav_..." ID
    private String status;
    private FundAccountResponse fund_account;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FundAccountResponse {
        private String id; // The "fa_..." ID
    }
}