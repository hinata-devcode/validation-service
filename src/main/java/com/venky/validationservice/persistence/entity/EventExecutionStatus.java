package com.venky.validationservice.persistence.entity;

public enum EventExecutionStatus {
	    PENDING,          // Newly inserted event, ready to process

	    PROCESSING,       // Worker picked this event

	    RETRY_SCHEDULED,  // Failed but will retry later (nextRetryAt is set)

	    COMPLETED,        // Successfully processed

	    DEAD_LETTER       // Permanently failed after max retries
}
