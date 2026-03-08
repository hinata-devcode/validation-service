package com.venky.validationservice.integration.razorpay;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RazorpayValidationCollectionResponse {

    private String entity;
    private int count;
    private List<RazorpayValidationItem> items;
} 