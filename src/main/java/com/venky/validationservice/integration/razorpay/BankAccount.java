package com.venky.validationservice.integration.razorpay;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccount {
    private final String name;
    private final String ifsc;
    private final String accountNumber;
}
