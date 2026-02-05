package com.venky.validationservice.integration.razorpay;

import com.venky.validationservice.controller.dto.BankAccountRequestDTO;
import com.venky.validationservice.controller.dto.UserDetailsDTO;
import com.venky.validationservice.controller.dto.VpaRequestDTO;
import com.venky.validationservice.dao.ValidationRepository;
import com.venky.validationservice.domain.model.FundAccountDetails;
import com.venky.validationservice.domain.model.ValidationResult;
import com.venky.validationservice.domain.service.ProviderValidationPort;
import com.venky.validationservice.exception.*;
import com.venky.validationservice.integration.common.ExecutionStatus;
import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.integration.common.ValidationExecutionResult;
import com.venky.validationservice.integration.common.ValidationState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
public class RazorpayValidationService implements ProviderValidationPort{

    private final RzpFundAccountFactory fundAccountFactory;
    private final RzpRequestFactory requestFactory;
    private final RzpClient rzpClient;

    public RazorpayValidationService(
            RzpFundAccountFactory fundAccountFactory,
            RzpRequestFactory requestFactory,
            RzpClient rzpClient) {
        this.fundAccountFactory = fundAccountFactory;
        this.requestFactory = requestFactory;
        this.rzpClient = rzpClient;
    }

    @Override
    public ValidationExecutionResult validate(FundAccountDetails details, ValidationState validationState) {

        FundAccount fundAccount =
                fundAccountFactory.createFundAccountDetails(details);

        RazorpayExternalRequest request =
                requestFactory.build(fundAccount);
        
        RazorpayResponse response =
        rzpClient.validateFundAccount(request);
        
        validationState.setProvider(Provider.RAZORPAY);
        validationState.setProviderReferenceId(response.getValidationId());
        validationState.setExecutionStatus(ExecutionStatus.PENDING);

        return new ValidationExecutionResult(
                validationState,
                Optional.empty()
        );
    }
}

