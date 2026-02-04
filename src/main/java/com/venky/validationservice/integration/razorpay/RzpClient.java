package com.venky.validationservice.integration.razorpay;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RzpClient {

    private final RestTemplate restTemplate;
    private final RazorpayProperties properties;

    public RzpClient(RestTemplate restTemplate, RazorpayProperties properties) {
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

        } catch (Exception ex) {
            // Intentionally simple placeholder
            return RazorpayResponse.error(
                    "Error handling not implemented yet"
            );
        }
    }
}
