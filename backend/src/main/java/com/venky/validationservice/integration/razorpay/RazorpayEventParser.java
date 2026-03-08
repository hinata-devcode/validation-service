package com.venky.validationservice.integration.razorpay;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.venky.validationservice.application.worker.ProviderResult;
import com.venky.validationservice.exception.NonRetryableProviderException;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ProviderValidationStatus;
import com.venky.validationservice.integration.razorpay.webhook.RazorpayWebhookSanitizer;
import com.venky.validationservice.integration.webhook.ProviderEventParser;


@Component
public class RazorpayEventParser implements ProviderEventParser{

    private final ObjectMapper mapper = new ObjectMapper();
    private final RazorpayWebhookSanitizer sanitizer;

    public RazorpayEventParser(RazorpayWebhookSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    @Override
    public ProviderResult parseWebhook(String rawPayload) {

        try {

            JsonNode root = mapper.readTree(rawPayload);

            // Navigate to entity node
            JsonNode entityNode =
                    root.path("payload")
                        .path("fund_account.validation")
                        .path("entity");

            if (entityNode.isMissingNode() || entityNode.isNull()) {
                throw new NonRetryableProviderException("Invalid webhook structure: entity not found",Provider.RAZORPAY);
            }

            String favId = entityNode.path("id").asText();
            String status = entityNode.path("status").asText();

            JsonNode validationResults = entityNode.path("validation_results");

            Map<String, String> attributes = new HashMap<>();

            String providerAccountStatus = null;
            Boolean accountActive = null;
            String nameMatchScore = null;
            String registeredName = null;
            String bankDetailsJson = null;

            if (validationResults != null && validationResults.isObject()) {

                ObjectNode obj = (ObjectNode) validationResults;

                obj.fields().forEachRemaining(e -> {

                    String key = e.getKey();

                    // Skip structured fields
                    if ("account_status".equals(key)
                            || "name_match_score".equals(key)
                            || "registered_name".equals(key)
                            || "bank_account".equals(key)) {
                        return;
                    }

                    JsonNode value = e.getValue();
                    attributes.put(
                            key,
                            value == null || value.isNull() ? null : value.asText()
                    );
                });

                // Structured fields
                providerAccountStatus = validationResults.path("account_status").asText(null);

                if (providerAccountStatus != null) {
                    accountActive = "active".equalsIgnoreCase(providerAccountStatus);
                }

                JsonNode nameMatchNode = validationResults.path("name_match_score");
                if (!nameMatchNode.isMissingNode() && !nameMatchNode.isNull()) {
                    nameMatchScore = nameMatchNode.asText();
                }

                JsonNode registeredNameNode = validationResults.path("registered_name");
                if (!registeredNameNode.isMissingNode() && !registeredNameNode.isNull()) {
                    registeredName = registeredNameNode.asText();
                }

                // This exists only in VPA case
                JsonNode bankDetailsNode = validationResults.path("bank_account");
                if (!bankDetailsNode.isMissingNode() && !bankDetailsNode.isNull()) {
                    bankDetailsJson = bankDetailsNode.toString(); // store raw JSON
                }
            }

            return new ProviderResult(
                    favId,
                    assingDomainStatus(status),
                    attributes,
                    sanitizer.sanitize(rawPayload),
                    accountActive,
                    nameMatchScore,
                    registeredName,
                    bankDetailsJson,
                    providerAccountStatus                   
            );

        } catch (Exception ex) {
            throw new NonRetryableProviderException("Failed to parse Razorpay webhook", ex,Provider.RAZORPAY);
        }
    }

    
	@Override
	public ProviderResult parseApiResponse(String rawPayload) {

		try {

			JsonNode root = mapper.readTree(rawPayload);

			String favId = root.path("id").asText();
			String status = root.path("status").asText();

			JsonNode validationResults = root.path("validation_results");
			JsonNode fundAccountNode = root.path("fund_account");

			Map<String, String> attributes = new HashMap<>();

			String providerAccountStatus = null;
			Boolean accountActive = null;
			String nameMatchScore = null;
			String registeredName = null;
			String bankDetailsJson = null;

			if (validationResults != null && validationResults.isObject()) {

				ObjectNode obj = (ObjectNode) validationResults;

				obj.fields().forEachRemaining(e -> {

					String key = e.getKey();

					// Skip structured fields
					if ("account_status".equals(key) || "name_match_score".equals(key)
							|| "registered_name".equals(key)) {
						return;
					}

					JsonNode value = e.getValue();
					attributes.put(key, value == null || value.isNull() ? null : value.asText());
				});

				providerAccountStatus = validationResults.path("account_status").asText(null);

				if (providerAccountStatus != null) {
					accountActive = "active".equalsIgnoreCase(providerAccountStatus);
				}

				JsonNode nameMatchNode = validationResults.path("name_match_score");
				if (!nameMatchNode.isMissingNode() && !nameMatchNode.isNull()) {
					nameMatchScore = nameMatchNode.asText();
				}

				JsonNode registeredNameNode = validationResults.path("registered_name");
				if (!registeredNameNode.isMissingNode() && !registeredNameNode.isNull()) {
					registeredName = registeredNameNode.asText();
				}
				
				JsonNode bankDetailsNode = validationResults.path("bank_account");
				if (!bankDetailsNode.isMissingNode() && !bankDetailsNode.isNull()) {
					bankDetailsJson = bankDetailsNode.toString(); // store raw JSON
				}
			}


			return new ProviderResult(favId, assingDomainStatus(status), attributes, sanitizer.sanitize(rawPayload),
					accountActive, nameMatchScore, registeredName, bankDetailsJson, providerAccountStatus);

		} catch (Exception ex) {
			throw new NonRetryableProviderException("Failed to parse Razorpay response", ex,Provider.RAZORPAY);
		}
	}


	@Override
	public Provider getProvider() {
		return Provider.RAZORPAY;
	}
	
	private ProviderValidationStatus assingDomainStatus(String status) {

		if (RazorpayValidationStatus.CREATED.name().equalsIgnoreCase(status))
			return ProviderValidationStatus.CREATED;
		else if (RazorpayValidationStatus.FAILED.name().equalsIgnoreCase(status))
			return ProviderValidationStatus.FAILED;
		else if (RazorpayValidationStatus.COMPLETED.name().equalsIgnoreCase(status))
			return ProviderValidationStatus.COMPLETED;

		throw new NonRetryableProviderException("Unknown Razorpay status: " + status,Provider.RAZORPAY);
	}

}
