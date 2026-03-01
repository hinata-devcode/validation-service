package com.venky.validationservice.reconciliation;

import java.util.List;

import com.venky.validationservice.integration.common.Provider;
import com.venky.validationservice.persistence.entity.ValidationRequestEntity;

public interface ProviderReconciliationHandler {

	Provider getProvider();
	void reconcile(List<ValidationRequestEntity> requests);

}
