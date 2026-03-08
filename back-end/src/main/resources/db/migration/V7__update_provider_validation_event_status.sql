-- Step 1: Convert ENUM column to VARCHAR to avoid future migration issues
ALTER TABLE provider_validation_event
MODIFY provider_status VARCHAR(50) NOT NULL;

-- Step 2: Migrate old values to new ones

UPDATE provider_validation_event
SET provider_status = 'DEAD_LETTER'
WHERE provider_status = 'FAILED';

UPDATE provider_validation_event
SET provider_status = 'RETRY_SCHEDULED'
WHERE provider_status = 'SKIPPED';
