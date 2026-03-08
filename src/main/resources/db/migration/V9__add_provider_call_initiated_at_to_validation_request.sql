-- V12__add_provider_call_initiated_at_to_validation_request.sql

ALTER TABLE validation_request
ADD COLUMN provider_call_initiated_at DATETIME(6) NULL;