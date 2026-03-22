package com.venky.validationservice.integration.common;

public enum ExecutionStatus {
	INITIATED,               // DB row created
	PROCESSING,              //When provider_reference_id is received
	PROVIDER_CALL_TIMEOUT,  // Call uncertain (read timeout)
	COMPLETED,              // Final success
	FAILED                 // Final failure
}

