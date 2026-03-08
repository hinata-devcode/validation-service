package com.venky.validationservice.integration.webhook;

import com.venky.validationservice.application.worker.ProviderResult;
import com.venky.validationservice.integration.common.Provider;

public interface ProviderEventParser {

	Provider getProvider();

	ProviderResult parseWebhook(String payload);

	ProviderResult parseApiResponse(String payload);

}
