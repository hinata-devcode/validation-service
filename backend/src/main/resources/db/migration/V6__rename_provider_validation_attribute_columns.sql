ALTER TABLE provider_validation_attributes
    CHANGE COLUMN attribute_key metadata_key VARCHAR(255) NOT NULL,
    CHANGE COLUMN attribute_value metadata_value VARCHAR(255) NULL;
