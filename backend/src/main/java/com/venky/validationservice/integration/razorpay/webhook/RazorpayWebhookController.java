package com.venky.validationservice.integration.razorpay.webhook;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.service.ProviderValidationEventPersistenceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/razorpay")
public class RazorpayWebhookController {

    private final RazorpayWebhookValidator validator;
    private final ProviderValidationEventPersistenceService eventService;
    private final RazorpayWebhookPayloadExtractor extractor;
    private final RazorpayWebhookSanitizer sanitizer;

    public RazorpayWebhookController(
            RazorpayWebhookValidator validator,
            ProviderValidationEventPersistenceService eventService,
            RazorpayWebhookPayloadExtractor extractor,
            RazorpayWebhookSanitizer sanitizer) {
        this.validator = validator;
        this.eventService = eventService;
        this.extractor = extractor;
        this.sanitizer = sanitizer;
    }

    @PostMapping
    public ResponseEntity<Void> consumeWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
			@RequestBody byte[] rawBody) {

		try {
			
			log.info("Received Razorpay webhook");

			// 1️⃣ Validate signature (NO parsing yet)
			validator.validate(rawBody, signature);

			// 2️⃣ Convert to String only AFTER validation
			String payload = new String(rawBody, StandardCharsets.UTF_8);

			// 3️⃣ Extract provider reference id (fav_xxx)
			String providerReferenceId = extractor.extractValidationId(payload);

			// 4️⃣ Sanitize payload (PII-safe)
			// String sanitizedPayload = sanitizer.sanitize(payload);

			log.info(" Razorpay webhook signature verified. providerRefId={}", providerReferenceId);

			// 5️⃣ Persist event (NO domain logic)
			eventService.recordWebhookEvent(Provider.RAZORPAY, providerReferenceId, payload);

			return ResponseEntity.ok().build();
		} catch (SecurityException ex) {

			log.warn("Invalid Razorpay webhook signature. Possible spoofed request ");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
}
