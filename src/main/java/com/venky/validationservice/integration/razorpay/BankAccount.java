package com.venky.validationservice.integration.razorpay;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccount {
    private final String name;
    private final String ifsc;
    @JsonProperty("account_number")
    private final String accountNumber;
}
