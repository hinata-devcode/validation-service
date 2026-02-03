package com.venky.validationservice.integration.razorpay;

import com.venky.validationservice.controller.dto.BankAccountRequestDTO;
import com.venky.validationservice.controller.dto.UserDetailsDTO;
import com.venky.validationservice.controller.dto.VpaRequestDTO;
import com.venky.validationservice.dao.ValidationRepository;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.domain.service.ProviderValidationPort;
import com.venky.validationservice.exception.*;
import com.venky.validationservice.integration.common.ProviderValidationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Slf4j
public class RazorpayValidationService implements ProviderValidationPort {

	private final RestTemplate restTemplate;
	private final ValidationRepository repository;
	
	public RazorpayValidationService(RestTemplate restTemplate, ValidationRepository repository) {
		super();
		this.restTemplate = restTemplate;
		this.repository = repository;
	}

	@Value("${razorpay.api.url:https://api.razorpay.com/v1/fund_accounts/validation}")
	private String razorpayUrl;

	@Value("${razorpay.source_account}")
	private String sourceAccountNumber;

	@Override
	public ValidationResult validateBankAccount(BankAccountRequestDTO request, UserDetailsDTO userDetailsDTO) {

		String internalId = "req_" + UUID.randomUUID();
		log.info("Starting Validation Request: {}", internalId);

		// 1. Adapter: Convert Generic DTO -> Razorpay Specific JSON
		RazorpayExternalRequest externalReq = mapToRazorpay(request, internalId);

		try {
			// 2. Network Call with Timeout (via RestTemplate)
			ResponseEntity<RazorpayResponse> response = restTemplate.postForEntity(razorpayUrl, externalReq,
					RazorpayResponse.class);

			// 3. Extract IDs
			String favId = response.getBody().getId(); // fav_123
			String faId = response.getBody().getFund_account().getId(); // fa_456

			// 4. Save PENDING State to DB
			saveToDb(internalId, favId, faId, request.getAccountNumber());

			return new ValidationResult(favId, "PENDING");

		} catch (HttpClientErrorException e) {
			log.error("Razorpay 4xx Error: {}", e.getResponseBodyAsString());
			throw new InvalidInputException("Bank details rejected: " + e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Razorpay 5xx/Timeout: {}", e.getMessage());
			throw new SystemBusyException("Banking partner unavailable. Please try again.");
		}
	}

	private RazorpayExternalRequest mapToRazorpay(BankAccountRequestDTO dto, String refId) {
		return RazorpayExternalRequest.builder().sourceAccountNumber(sourceAccountNumber).validationType("optimized")
				.referenceId(refId)
				.fundAccount(RazorpayExternalRequest.FundAccount.builder().accountType("bank_account")
						.bankAccount(RazorpayExternalRequest.BankAccount.builder().name(dto.getUser().getName())
								.ifsc(dto.getIfsc()).accountNumber(dto.getAccountNumber()).build())
						.contact(RazorpayExternalRequest.Contact.builder().name(dto.getUser().getName())
								.email(dto.getUser().getEmail()).contact(dto.getUser().getPhone()).type("customer")
								.referenceId(refId).build())
						.build())
				.build();
	}

	private void saveToDb(String internalId, String favId, String faId, String rawAccount) {
		ValidationEntity entity = new ValidationEntity();
		entity.setRequestId(internalId);
		entity.setProviderId(favId); // Validation ID
		entity.setFundAccountId(faId); // Fund Account ID
		entity.setStatus("PENDING");
		entity.setMaskedData("XXXX" + rawAccount.substring(rawAccount.length() - 4));
		repository.save(entity);
	}

	@Override
	public ValidationResult validateVpa(VpaRequestDTO request, UserDetailsDTO userDetailsDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProviderValidationResult validate(FundAccountDetails details) {
		// TODO Auto-generated method stub
		return null;
	}

}
