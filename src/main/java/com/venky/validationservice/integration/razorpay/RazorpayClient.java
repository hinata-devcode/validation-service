package com.venky.validationservice.integration.razorpay;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.utils.RetryExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	
	public RazorpayValidationCollectionResponse fetchValidations(long from, long to, int count, int skip) {
		return retryExecutor.execute(() -> fetchValidationCollection(from, to, count, skip), 3, Duration.ofSeconds(2));
	}

	private String createValidationRequest(RazorpayExternalRequest request) {

		try {
			HttpEntity<RazorpayExternalRequest> entity = createHttpEntity(request);

			String url = properties.getBaseUrl() + "/v1/fund_accounts/validations";
//
//			SimpleClientHttpRequestFactory factory =
//		            new SimpleClientHttpRequestFactory();
//
//		    factory.setConnectTimeout(5000);
//		    factory.setReadTimeout(200);
//
//		    RestTemplate restTemplate= new RestTemplate(factory);
//		  //  code tested for timed out cases

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

			return response.getBody();
		} catch (HttpClientErrorException ex) {
			String errorBody = ex.getResponseBodyAsString();
			HttpStatusCode status = ex.getStatusCode();

			log.error("Razorpay HTTP Client Error (4xx). Status: {}, Body: {}", status, errorBody, ex.getMessage());

			throw new NonRetryableProviderException(
					String.format("Razorpay client error: status=%s body=%s", status, errorBody), ex,
					Provider.RAZORPAY);

		} catch (HttpServerErrorException ex) {
			log.error("Razorpay HTTP Server Error (5xx). Status: {}, Body: {}", ex.getStatusCode(),
					ex.getResponseBodyAsString(), ex.getMessage());

			throw new RetryableProviderException("Razorpay server error", ex, Provider.RAZORPAY);

		} catch (ResourceAccessException ex) {
			log.warn("Network access exception while calling Razorpay", ex.getMessage());
			throw classifyNetworkException(ex);
		}
	}


	private String fetchValidationStatus(String providerReferenceId) {

		try {

			HttpEntity<String> entity = createHttpEntity(providerReferenceId);

			String url = properties.getBaseUrl() + "/v1/fund_accounts/validations/" + providerReferenceId;

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			return response.getBody();
		} catch (HttpClientErrorException ex) {
			String errorBody = ex.getResponseBodyAsString();
			HttpStatusCode status = ex.getStatusCode();
			log.error("Razorpay HTTP Client Error (4xx). Status: {}, Body: {}", status, errorBody, ex.getMessage());
			throw new NonRetryableProviderException("Razorpay client error " + ex.getMessage(), ex, Provider.RAZORPAY);

		} catch (HttpServerErrorException ex) {
			log.error("Razorpay HTTP Server Error (5xx). Status: {}, Body: {}", ex.getStatusCode(),
					ex.getResponseBodyAsString(), ex.getMessage());
			throw new RetryableProviderException("Razorpay server error " + ex.getMessage(), ex, Provider.RAZORPAY);

		} catch (ResourceAccessException ex) {
			log.warn("Network access exception while calling Razorpay", ex.getMessage());
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
			return new NonRetryableProviderException("Unknown network error", ex,Provider.RAZORPAY);
		}

		if (root instanceof ConnectException || root instanceof UnknownHostException) {
			return new RetryableProviderException("Connection failure", ex,Provider.RAZORPAY);
		}

		if (root instanceof SocketTimeoutException) {
			return new ProviderCallTimeoutException("Read timeout", ex,Provider.RAZORPAY);
		}

		return new NonRetryableProviderException("Unknown network error", ex,Provider.RAZORPAY);
	}
	
	private RazorpayValidationCollectionResponse fetchValidationCollection(long from, long to, int count, int skip) {

		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(properties.getApiKey(), properties.getApiSecret());

			HttpEntity<Void> entity = new HttpEntity<>(headers);

			String url = properties.getBaseUrl() + "/v1/fund_accounts/validations" + "?account_number="
					+ properties.getSourceAccountNumber() + "&from=" + from + "&to=" + to + "&count=" + count + "&skip="
					+ skip;

			ResponseEntity<RazorpayValidationCollectionResponse> response = restTemplate.exchange(url, HttpMethod.GET,
					entity, RazorpayValidationCollectionResponse.class);

			return response.getBody();

		} catch (HttpClientErrorException ex) {
			String errorBody = ex.getResponseBodyAsString();
			HttpStatusCode status = ex.getStatusCode();
			log.error("Razorpay HTTP Client Error (4xx). Status: {}, Body: {}", status, errorBody, ex.getMessage());
			throw new NonRetryableProviderException("Razorpay client error " + ex.getMessage(), ex, Provider.RAZORPAY);

		} catch (HttpServerErrorException ex) {
			log.error("Razorpay HTTP Server Error (5xx). Status: {}, Body: {}", ex.getStatusCode(),
					ex.getResponseBodyAsString(), ex.getMessage());
			throw new RetryableProviderException("Razorpay server error " + ex.getMessage(), ex, Provider.RAZORPAY);

		} catch (ResourceAccessException ex) {
			log.warn("Network access exception while calling Razorpay", ex.getMessage());
			throw classifyNetworkException(ex);
		}
	}

}
