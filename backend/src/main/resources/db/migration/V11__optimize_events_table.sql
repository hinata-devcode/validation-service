CREATE INDEX idx_valreq_provider_ref 
ON validation_request (provider_reference_id);

CREATE INDEX idx_valreq_polling 
ON validation_request (execution_status, provider_reference_id, last_status_check_at);

CREATE INDEX idx_valreq_stuck_cases 
ON validation_request (execution_status, provider_reference_id, provider_call_initiated_at);

CREATE INDEX idx_event_req_status 
ON provider_validation_event (validation_request_id, provider_status);

CREATE INDEX idx_provider_event_worker 
ON provider_validation_event (provider_status, next_retry_at, created_at);