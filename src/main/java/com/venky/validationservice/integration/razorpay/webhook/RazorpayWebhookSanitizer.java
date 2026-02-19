package com.venky.validationservice.integration.razorpay.webhook;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Component
public class RazorpayWebhookSanitizer {

    private final ObjectMapper mapper = new ObjectMapper();

    public String sanitize(String payload) {
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(payload);

            JsonNode entity =
                root.at("/payload/fund_account.validation/entity");

            if (entity.isObject()) {
                ObjectNode entityObj = (ObjectNode) entity;

                mask(entityObj, "utr");
                mask(entityObj, "registered_name");

                JsonNode bankAccount =
                        entityObj.at("/validation_results/bank_account");
                if (bankAccount.isObject()) {
                    ((ObjectNode) bankAccount)
                            .put("account_number", "MASKED");
                }
            }

            return mapper.writeValueAsString(root);

        } catch (Exception e) {
            return "{\"sanitization\":\"failed\"}";
        }
    }

    private void mask(ObjectNode node, String field) {
        if (node.has(field)) {
            node.put(field, "MASKED");
        }
    }
}
