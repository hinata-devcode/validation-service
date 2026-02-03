package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class BankAccount {
    private String name;
    private String ifsc;
    @JsonProperty("account_number")
    private String accountNumber;
}
