package com.venky.validationservice.integration.razorpay;

import java.time.Duration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.venky.validationservice.exception.NonRetryableException;
import com.venky.validationservice.exception.RetryableException;
import com.venky.validationservice.integration.utils.RetryExecutor;

@Component
public class RazorpayClient {

	private final RestTemplate restTemplate;
	private final RazorpayProperties properties;
	private final RetryExecutor retryExecutor;

	public RazorpayClient(RestTemplate restTemplate, RazorpayProperties properties, RetryExecutor retryExecutor) {
		this.restTemplate = restTemplate;
		this.properties = properties;
		this.retryExecutor = retryExecutor;
	}

	public RazorpayResponse validateFundAccount(RazorpayExternalRequest request) {
		return retryExecutor.execute(() -> createValidationRequest(request), 3, Duration.ofSeconds(2));
	}

	public String fetchStatus(String providerReferenceId) {
		return retryExecutor.execute(() -> fetchValidationStatus(providerReferenceId), 3, Duration.ofSeconds(2));
	}

	private RazorpayResponse createValidationRequest(RazorpayExternalRequest request) {

		try {
			HttpEntity<RazorpayExternalRequest> entity = createHttpEntity(request);

			String url = properties.getBaseUrl() + "/v1/fund_accounts/validations";

			ResponseEntity<RazorpayResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
					RazorpayResponse.class);

			return response.getBody();
		} catch (HttpClientErrorException ex) {
	        throw new NonRetryableException("Razorpay client error", ex);

	    } catch (HttpServerErrorException ex) {
	        throw new RetryableException("Razorpay server error", ex);

	    } catch (ResourceAccessException ex) {
	        throw new RetryableException("Razorpay timeout", ex);
	    }
	}

	private String fetchValidationStatus(String providerReferenceId) {

		try {

			HttpEntity<String> entity = createHttpEntity(providerReferenceId);

			String url = properties.getBaseUrl() + "/v1/fund_accounts/validations/" + providerReferenceId;

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			return response.getBody();
		} 
		catch (HttpClientErrorException ex) {
	        throw new NonRetryableException("Razorpay client error", ex);

	    } catch (HttpServerErrorException ex) {
	        throw new RetryableException("Razorpay server error", ex);

	    } catch (ResourceAccessException ex) {
	        throw new RetryableException("Razorpay timeout", ex);
	    }
	}

	private <T> HttpEntity<T> createHttpEntity(T request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(properties.getApiKey(), properties.getApiSecret());

		HttpEntity<T> entity = new HttpEntity<>(request, headers);
		return entity;
	}

}
