package com.venky.validationservice.integration.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtil {

    public static String generateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes); 
        } catch (NoSuchAlgorithmException e) {
            // This should never happen unless the JVM is severely broken
            throw new RuntimeException("SHA-256 algorithm not found", e); 
        }
    }
}
