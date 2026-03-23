-- 1. Create Users Table (Parent)
CREATE TABLE `users` (
  `id` varchar(36) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`) 
  -- Removed redundant idx_users_username here
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Create User Roles Table (Child of users)
CREATE TABLE `user_roles` (
  `user_id` varchar(36) NOT NULL,
  `roles` varchar(50) NOT NULL,
  KEY `fk_user_roles_user_id` (`user_id`),
  CONSTRAINT `fk_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Create Validation Request Table
CREATE TABLE `validation_request` (
  `validation_request_id` char(36) NOT NULL,
  `confidence_level` varchar(50) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `execution_status` varchar(50) NOT NULL,
  `failure_reason` varchar(255) DEFAULT NULL,
  `first_created_at` datetime(6) DEFAULT NULL,
  `last_status_check_at` datetime(6) DEFAULT NULL,
  `poll_attempts` int DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `provider_reference_id` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `validation_status` varchar(50) DEFAULT NULL,
  `failure_origin` varchar(50) DEFAULT NULL,
  `provider_call_initiated_at` datetime(6) DEFAULT NULL,
  `idempotency_key` varchar(36) NOT NULL,
  `payload_hash` varchar(64) NOT NULL,
  PRIMARY KEY (`validation_request_id`),
  UNIQUE KEY `idempotency_key_UNIQUE` (`idempotency_key`),
  KEY `idx_valreq_provider_ref` (`provider_reference_id`),
  KEY `idx_valreq_polling` (`execution_status`,`provider_reference_id`,`last_status_check_at`),
  KEY `idx_valreq_stuck_cases` (`execution_status`,`provider_reference_id`,`provider_call_initiated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. Create Provider Validation Result Table
CREATE TABLE `provider_validation_result` (
  `received_at` datetime(6) DEFAULT NULL,
  `validation_request_id` char(36) NOT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `provider_reference_id` varchar(255) DEFAULT NULL,
  `provider_status` varchar(255) DEFAULT NULL,
  `sanitized_payload` json DEFAULT NULL,
  `provider_account_status` varchar(50) DEFAULT NULL,
  `provider_name_match_score` varchar(20) DEFAULT NULL,
  `provider_registered_name` varchar(255) DEFAULT NULL,
  `provider_account_active` tinyint(1) DEFAULT NULL,
  `provider_bank_details_json` json DEFAULT NULL,
  PRIMARY KEY (`validation_request_id`),
  UNIQUE KEY `uk_provider_provider_ref` (`provider`,`provider_reference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 5. Create Provider Validation Attributes Table (Child of Validation Result)
CREATE TABLE `provider_validation_attributes` (
  `validation_request_id` char(36) NOT NULL,
  `metadata_key` varchar(255) NOT NULL,
  `metadata_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`validation_request_id`,`metadata_key`),
  CONSTRAINT `fk_provider_validation_attributes_result` FOREIGN KEY (`validation_request_id`) REFERENCES `provider_validation_result` (`validation_request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 6. Create Provider Validation Event Table
CREATE TABLE `provider_validation_event` (
  `retry_count` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `next_retry_at` datetime(6) DEFAULT NULL,
  `last_error` varchar(255) DEFAULT NULL,
  `provider_reference_id` varchar(255) NOT NULL,
  `raw_payload` json DEFAULT NULL,
  `validation_request_id` varchar(255) DEFAULT NULL,
  `event_type` varchar(50) NOT NULL,
  `provider` varchar(50) NOT NULL,
  `provider_status` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_event_req_status` (`validation_request_id`,`provider_status`),
  KEY `idx_provider_event_worker` (`provider_status`,`next_retry_at`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;