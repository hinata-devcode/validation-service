package com.venky.validationservice.integration.razorpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.venky.validationservice.controller.dto.BankAccountRequestDTO;
import com.venky.validationservice.controller.dto.UserDetailsDTO;
import com.venky.validationservice.controller.dto.VpaRequestDTO;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.domain.service.ProviderValidationPort;
import com.venky.validationservice.exception.*;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ValidationState;
import com.venky.validationservice.persistence.service.ProviderValidationEventPersistenceService;
import com.venky.validationservice.persistence.service.ValidationPersistenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RazorpayValidationService implements ProviderValidationPort {

	private final RazorpayAccountDetailsFactory accountDetailsFactory;
	private final RzpRequestFactory requestFactory;
	private final RazorpayClient rzpClient;
	private final ProviderValidationEventPersistenceService eventPersistence;
	private final ValidationPersistenceService validationPersistenceService;
	 private final ObjectMapper objectMapper;

	public RazorpayValidationService(RazorpayAccountDetailsFactory fundAccountFactory, RzpRequestFactory requestFactory,
			RazorpayClient rzpClient, ProviderValidationEventPersistenceService eventPersistenceService,
			ValidationPersistenceService validationPersistenceService, ObjectMapper objectMapper) {
		this.accountDetailsFactory = fundAccountFactory;
		this.requestFactory = requestFactory;
		this.rzpClient = rzpClient;
		this.eventPersistence = eventPersistenceService;
		this.validationPersistenceService = validationPersistenceService;
		this.objectMapper = objectMapper;
	}
	
	@Override
    public Provider getProviderName() {
        return Provider.RAZORPAY;
    }

	@Override
	public void validate(FundAccountDetails details, ValidationState validationState) {

		Contact contact=accountDetailsFactory.createContactDetails(details);
		FundAccount fundAccount = accountDetailsFactory.createFundAccountDetails(details,contact);
		
		RazorpayExternalRequest request = requestFactory.build(fundAccount,validationState.getValidationRequestId());

		String rawJson  = rzpClient.validateFundAccount(request);
		
		RazorpayResponse parsedResponse;
		try {
			parsedResponse = objectMapper.readValue(rawJson, RazorpayResponse.class);
		} catch (JsonProcessingException e) {
			  log.error("Failed to parse Razorpay response for validationRequestId: {}. Raw JSON: {}", validationState.getValidationRequestId(), rawJson, e);
			throw new NonRetryableProviderException("parsing exception",e,Provider.RAZORPAY);
		}

		validationState.setProvider(Provider.RAZORPAY);
		
		log.info("Razorpay successfully created FAV for validationRequestId: {}. ProviderReferenceId (FAV ID): {}", 
		          validationState.getValidationRequestId(), parsedResponse.getValidationId());
		
		validationState.setProviderReferenceId(parsedResponse.getValidationId());
		
		String favId = parsedResponse.getValidationId();

		eventPersistence.recordApiResponse(validationState.getValidationRequestId(), Provider.RAZORPAY, favId,
				rawJson);

		// this is for my DB
		validationPersistenceService.updateValidationEventWithProvderDetails(validationState.getValidationRequestId(),
				Provider.RAZORPAY, favId);

		// this is for my domain layer/UI return state
		validationState.markProcessing(Provider.RAZORPAY, favId);

	}
}
