-- V8: Convert ENUM columns to VARCHAR(50)

ALTER TABLE validation_request
    MODIFY execution_status VARCHAR(50) NOT NULL;

ALTER TABLE validation_request
    MODIFY validation_status VARCHAR(50);

ALTER TABLE validation_request
    MODIFY confidence_level VARCHAR(50);

ALTER TABLE validation_request
    MODIFY failure_origin VARCHAR(50);