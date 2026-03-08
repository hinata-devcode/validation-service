package com.venky.validationservice.integration.razorpay;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "razorpay")
@ToString(exclude = {"apiKey", "apiSecret","sourceAccountNumber"})
public class RazorpayProperties {

	 /* ---------- Client config ---------- */
    private String baseUrl;
    private String apiKey;
    private String apiSecret;

    /* ---------- Validation defaults ---------- */
    private String sourceAccountNumber;
    private String validationType;
    
}

