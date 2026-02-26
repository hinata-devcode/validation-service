package com.venky.validationservice.integration.razorpay;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.venky.validationservice.exception.NonRetryableProviderException;
import com.venky.validationservice.exception.ProviderCallTimeoutException;
import com.venky.validationservice.exception.RetryableProviderException;
import com.venky.validationservice.integration.utils.RetryExecutor;

@Component
public class RazorpayClient {

	private final RestTemplate restTemplate;
	private final RazorpayProperties properties;
	private final RetryExecutor retryExecutor;
	private final int maxAttempts=3;
	private final int initialDelay=2;

	public RazorpayClient(RestTemplate restTemplate, RazorpayProperties properties, RetryExecutor retryExecutor) {
		this.restTemplate = restTemplate;
		this.properties = properties;
		this.retryExecutor = retryExecutor;
	}

	public String validateFundAccount(RazorpayExternalRequest request) {
		return retryExecutor.execute(() -> createValidationRequest(request), maxAttempts, Duration.ofSeconds(initialDelay));
	}

	public String fetchStatus(String providerReferenceId) {
		return retryExecutor.execute(() -> fetchValidationStatus(providerReferenceId), 3, Duration.ofSeconds(2));
	}

	private String createValidationRequest(RazorpayExternalRequest request) {

		try {
			HttpEntity<RazorpayExternalRequest> entity = createHttpEntity(request);

			String url = properties.getBaseUrl() + "/v1/fund_accounts/validations";
			
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity,
					String.class);

			return response.getBody();
		} catch (HttpClientErrorException ex) {
			String errorBody = ex.getResponseBodyAsString();
			HttpStatusCode status = ex.getStatusCode();

			throw new NonRetryableProviderException(
					String.format("Razorpay client error: status=%s body=%s", status, errorBody), ex);

		} catch (HttpServerErrorException ex) {
			//need to implement logging
	        throw new RetryableProviderException("Razorpay server error", ex);

	    } catch (ResourceAccessException ex) {
	    	throw classifyNetworkException(ex);
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
	        throw new NonRetryableProviderException("Razorpay client error "+ex.getMessage(), ex);

	    } catch (HttpServerErrorException ex) {
	        throw new RetryableProviderException("Razorpay server error "+ex.getMessage(), ex);

	    } catch (ResourceAccessException ex) {
	    	throw classifyNetworkException(ex);
	    }
	}

	private <T> HttpEntity<T> createHttpEntity(T request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(properties.getApiKey(), properties.getApiSecret());

		HttpEntity<T> entity = new HttpEntity<>(request, headers);
		return entity;
	}
	
	private RuntimeException classifyNetworkException(ResourceAccessException ex) throws ProviderCallTimeoutException {
		Throwable root = ex.getRootCause();

		if (root == null) {
			return new NonRetryableProviderException("Unknown network error", ex);
		}

		if (root instanceof ConnectException || root instanceof UnknownHostException) {
			return new RetryableProviderException("Connection failure", ex);
		}

		if (root instanceof SocketTimeoutException) {
			return new ProviderCallTimeoutException("Read timeout", ex);
		}

		return new NonRetryableProviderException("Unknown network error", ex);
	}
	
}
