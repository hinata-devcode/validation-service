package com.venky.validationservice.integration.razorpay.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RazorpayWebhookValidator {

	 @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    public void validate(byte[] rawBody, String receivedSignature) {
        String expectedSignature = computeHmacSha256(rawBody, webhookSecret);

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Invalid Razorpay webhook signature");
        }
    }

    private String computeHmacSha256(byte[] data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(data);
            return HexFormat.of().formatHex(rawHmac);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to validate webhook", e);
        }
    }
}
