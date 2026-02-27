package com.venky.validationservice.reconciliation;

import java.util.List;

import com.venky.validationservice.persistence.entity.ValidationRequestEntity;

public interface ProviderReconciliationHandler {

	String getProvider();
	void reconcile(List<ValidationRequestEntity> requests);

}
