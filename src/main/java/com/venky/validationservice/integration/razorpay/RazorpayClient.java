package com.venky.validationservice.integration.razorpay;


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

@Component
public class RazorpayClient {

    private final RestTemplate restTemplate;
    private final RazorpayProperties properties;

    public RazorpayClient(RestTemplate restTemplate, RazorpayProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public RazorpayResponse validateFundAccount(RazorpayExternalRequest request) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(
                    properties.getApiKey(),
                    properties.getApiSecret()
            );

            HttpEntity<RazorpayExternalRequest> entity =
                    new HttpEntity<>(request, headers);

            String url = properties.getBaseUrl() + "/v1/fund_accounts/validations";

            ResponseEntity<RazorpayResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            RazorpayResponse.class
                    );

            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new RzpException("Razorpay API error", ex);

        } catch (ResourceAccessException ex) {
            throw new RzpException("Razorpay API timeout", ex);
        }
    }

	public String fetchStatus(String providerReferenceId) {
		// TODO Auto-generated method stub
		return null;
	}
}
