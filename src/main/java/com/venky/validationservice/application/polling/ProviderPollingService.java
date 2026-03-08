package com.venky.validationservice.application.polling;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;

public interface ProviderPollingService {

	Provider getProvider();

	void poll(ValidationRequestEntity request);
}
