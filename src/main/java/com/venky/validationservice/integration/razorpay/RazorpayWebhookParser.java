package com.venky.validationservice.integration.razorpay;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.razorpay.webhook.RazorpayWebhookSanitizer;


@Component
public class RazorpayWebhookParser {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RazorpayWebhookSanitizer sanitizer;

    public RazorpayWebhookParser(RazorpayWebhookSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public RazorpayWebhookResult parse(String rawPayload) {
        try {
            JsonNode root = mapper.readTree(rawPayload);
            JsonNode entity =
                root.at("/payload/fund_account.validation/entity");

            String favId = entity.get("id").asText();
            String status = entity.get("status").asText();
            
            Map<String, String> attributes = new HashMap<>();

            JsonNode validationResults = entity.get("validation_results");

            if (validationResults != null && validationResults.isObject()) {
                ObjectNode obj = (ObjectNode) validationResults;

                obj.fields().forEachRemaining(e -> {
                    JsonNode value = e.getValue();
                    attributes.put(
                        e.getKey(),
                        value.isValueNode() ? value.asText() : value.toString()
                    );
                });
            }

            return new RazorpayWebhookResult(
                    favId,
                    status,
                    attributes,
                    sanitizer.sanitize(rawPayload)
            );

        } catch (Exception e) {
            throw new IllegalStateException("Invalid webhook payload", e);
        }
    }
}
