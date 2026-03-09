![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-Backend-green)
![Architecture](https://img.shields.io/badge/Architecture-Async-blue)


# Financial Validation Engine

A **fault-tolerant validation system** built using **Java and Spring Boot** to validate bank accounts or VPAs through external payment providers.

The system is designed to handle **asynchronous workflows, webhook callbacks, retries, and failure recovery**, similar to real financial backend systems.

---

# Project Demo

This video demonstrates the complete validation lifecycle including request submission, async processing, provider integration, webhook handling, and final UI response.

Click the image below to watch the demo:

[![Watch Demo](images/demo-thumbnail.png)](PASTE_YOUR_GOOGLE_DRIVE_VIDEO_LINK_HERE)

The demo shows the full lifecycle:

1. User login
2. Validation request submission
3. Async worker processing
4. Provider API integration
5. Webhook callback handling
6. Final validation result in UI

---

# System Architecture

![Architecture Diagram](images/architecture.png)

### High Level Flow

```
UI
↓
Spring Boot API
↓
Validation Service
↓
Background Worker
↓
External Provider API
↓
Webhook Handler
↓
Event Processor
↓
Database
↓
UI Polling Result
```

The system processes validation requests **asynchronously**, ensuring that the user interface remains responsive while external provider validations are completed.

---

# Validation Lifecycle

Each validation request moves through defined states.

```
INITIATED
↓
PROCESSING
↓
COMPLETED / FAILED / PROVIDER_CALL_TIMEOUT
```

This state-based design ensures safe retries and prevents duplicate processing.

---

# Request Processing Flow

```
User Request
↓
Spring Boot Controller
↓
Validation Service
↓
Persist Validation Request
↓
Background Worker Picks Task
↓
Provider API Call
↓
Provider Sends Webhook
↓
Event Processor Updates Result
↓
Database Updated
↓
UI Polling Returns Final Status
```

This architecture allows the system to handle **long-running validations without blocking the user request.**

---

# Reliability & Failure Handling

External provider integrations are unpredictable.
The system includes multiple fallback mechanisms.

### Webhook Failure Recovery

If a webhook is delayed or missed:

```
Polling Scheduler
↓
Check validation status
↓
Update database
```

---

### Provider Timeout Recovery

If a provider call fails or times out:

```
Reconciliation Scheduler
↓
Retry pending validations
↓
Update final status
```

These mechanisms ensure **eventual consistency** and reliable validation results.

---

# Security

The system includes multiple security layers.

**Authentication**

* Spring Security
* JWT based authentication

**Webhook Protection**

* HMAC signature verification
* Ensures callbacks originate from trusted providers

---

# Observability

The system includes structured logging to trace the full lifecycle of validation requests.

Logs include:

* validation request creation
* worker processing
* provider API calls
* webhook events
* final validation persistence

This allows developers to **trace each validation request from start to finish.**

---

# Example UI Responses

### Successful Validation

(Add screenshot here)

---

### Invalid Input Handling

(Add screenshot here)

Examples include:

* Invalid VPA format
* Invalid account number
* Missing required fields

---

# Tech Stack

Backend

* Java
* Spring Boot
* Spring Security
* JPA / Hibernate

Database

* MySQL

Integration

* External Payment Provider API
* Webhook Callbacks

Async Processing

* Background Workers
* Scheduler Jobs

---

# Backend Engineering Concepts Demonstrated

This project demonstrates several real-world backend patterns:

* Asynchronous request processing
* Webhook driven architecture
* Background worker processing
* Scheduler based failure recovery
* Idempotent validation handling
* API security using JWT
* Event-driven validation lifecycle

---

# Failure Scenarios & System Handling

| Scenario                | System Handling                             |
| ----------------------- | ------------------------------------------- |
| Provider API timeout    | Reconciliation scheduler retries validation |
| Webhook not received    | Polling scheduler fetches status            |
| Duplicate request       | Database locking prevents race conditions   |
| Invalid validation data | API validation prevents processing          |

---

# Future Improvements

Possible enhancements for production environments:

* Message queues (Kafka / RabbitMQ)
* Distributed worker scaling
* Circuit breaker for provider APIs
* Monitoring dashboards
* Rate limiting for API protection

---

# Author

Venkatesh
Backend Developer
