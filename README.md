![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-Backend-green)
![Architecture](https://img.shields.io/badge/Architecture-Async%20%7C%20Distributed-blue)
![Status](https://img.shields.io/badge/Project-Backend%20Architecture-brown)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED)
![AWS](https://img.shields.io/badge/AWS-Deployed-FF9900)

# Financial Validation Engine

A **fault-tolerant, distributed-ready financial validation system** built using **Java + Spring Boot** that validates **bank accounts or VPAs** through external payment providers.

The project demonstrates how **real financial backend systems handle asynchronous processing, distributed safety, provider integrations, and failure recovery** — designed to operate correctly even when scaled across multiple backend instances.

---

## 🚀 Project Overview

| Feature | Description |
|------|-------------|
| Architecture | Asynchronous, distributed-safe validation pipeline |
| Backend | Java + Spring Boot |
| Security | JWT Authentication + HMAC Webhook Validation |
| Processing | Background Worker Processing (multi-instance safe) |
| Integration | External Payment Provider APIs |
| Reliability | Circuit Breaker (Resilience4j) + Retry + Scheduler Recovery |
| Rate Limiting | Distributed Rate Limiting via Redis + Bucket4j |
| Database | MySQL |
| Deployment | Docker Compose + AWS (EC2) |

---

## 📑 Table of Contents

- [Project Demo](#project-demo)
- [System Architecture](#system-architecture)
- [Validation Lifecycle](#validation-lifecycle)
- [Request Processing Flow](#request-processing-flow)
- [Distributed System Capabilities](#distributed-system-capabilities)
- [Failure Handling](#failure-handling)
- [Security](#security)
- [UI Screenshots](#ui-screenshots)
- [Tech Stack](#tech-stack)
- [Local Setup with Docker](#local-setup-with-docker)
- [Backend Concepts Demonstrated](#backend-concepts-demonstrated)
- [Current Gaps & Future Improvements](#current-gaps--future-improvements)

---

## Project Demo

🎥 **Watch the System Demo**

👉 [Watch Demo Video](https://drive.google.com/file/d/1sAMhtqYEhAUSptF8p_Rug2sZc9L6B98m/view?usp=sharing)

The demo shows:

1. User login  
2. Validation request submission  
3. Async worker processing  
4. Provider API integration  
5. Webhook callback handling  
6. Final validation result in UI  

---

## System Architecture

![System Architecture](images/System_Architecture.png)

### High Level Flow

```
UI
↓
Spring Boot API (Multiple Instances)
↓
Validation Service
↓
Database (Shared)
↓
Background Workers (Distributed)
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

The system processes validations **asynchronously**, ensuring the UI remains responsive while external validations are completed. It is designed to operate consistently across **multiple horizontally scaled backend instances**.

---

## Validation Lifecycle

Each validation request moves through defined states.

```
INITIATED
↓
PROCESSING
↓
COMPLETED / FAILED / PROVIDER_CALL_TIMEOUT
```

This **state-driven design** prevents duplicate processing and supports safe retries — even when multiple backend instances are running concurrently.

---

## Request Processing Flow

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

This architecture ensures **non-blocking request processing** and **distributed-safe execution**.

---

## Distributed System Capabilities

This system is built to run correctly across **multiple backend instances** (e.g., 5+ Docker containers) without race conditions.

### Multi-Instance Safe Design
- No race conditions during validation processing, state transitions, or retry handling
- Single processing guaranteed per validation request
- Safe concurrent access to shared database resources

### Horizontal Scalability
- Each backend instance can independently accept API requests and process background jobs
- System remains consistent under scale

### Distributed Rate Limiting
- Uses **Bucket4j + Redis** (cloud-managed)
- Rate limits enforced uniformly across all instances — no instance can bypass throttling rules

---

## Failure Handling

External provider integrations can fail or respond slowly.  
The system includes **automatic recovery mechanisms**.

### Circuit Breaker (Resilience4j)
Protects the system from cascading failures when external providers are degraded:

```
Provider Call Fails Repeatedly
↓
Circuit Breaker OPENS
↓
Calls halted during cooldown
↓
Circuit Breaker recovers → Resumes calls
```

### Webhook Recovery

If a webhook is delayed or not received:

```
Polling Scheduler
↓
Detect missing callback
↓
Fetch status from provider
↓
Update database
```

### Provider Timeout Recovery

If a provider call fails:

```
Reconciliation Scheduler
↓
Retry pending validations
↓
Update final status
```

### Idempotency
- Prevents duplicate validation requests from being processed
- Ensures safe retries without financial side effects — critical for correctness

These mechanisms together ensure **eventual consistency** and **graceful degradation** under failure conditions.

---

## Security

### Authentication

- Spring Security
- JWT Authentication

### Webhook Protection

- HMAC Signature Verification
- Ensures callbacks originate from trusted providers only

### API Design

- Versioned APIs (`/v1/validate`) for backward compatibility
- Centralized global exception handling for consistent error responses

---

## UI Screenshots

### Dashboard

![Dashboard](images/dashboard.png)

### Validation Result (Valid)

![Validation Success Valid Fund Account Details](images/validation_valid_details.png)

### Validation Result (Invalid)

![Validation Success Invalid Fund Account Details](images/validation_invalid_details.png)

---

## Tech Stack

### Backend

- Java 17
- Spring Boot
- Spring Security
- JPA / Hibernate
- Resilience4j (Circuit Breaker)
- Bucket4j (Rate Limiting)

### Database & Cache

- MySQL (persistence)
- Redis (distributed rate limiting — cloud-managed)

### Integration

- External Payment Provider APIs
- Webhook callbacks (HMAC-verified)

### Async Processing

- Background workers (multi-instance safe)
- Scheduler-based recovery jobs

### Infrastructure

- Docker + Docker Compose
- AWS EC2

---

## Local Setup with Docker

You can run the full stack locally using Docker Compose. Follow these steps to get it running cleanly on your machine.

### Prerequisites

- [Docker](https://www.docker.com/products/docker-desktop) installed
- [Git](https://git-scm.com/) installed

### Step 1 — Clone the Repository

```bash
git clone <your-repo-url>
cd validation-service
```

### Step 2 — Configure Environment Variables

Copy the example environment file and fill in your values:

```bash
cp .env.example .env
```

Update `.env` with your provider API keys, JWT secret, and Redis credentials.

### Step 3 — Build and Start the Stack

```bash
docker compose up -d --build
```

- `--build` compiles the Java `.jar` and builds React assets fresh
- `-d` runs containers in detached (background) mode

### Step 4 — Verify Everything is Running

```bash
docker ps
```

You should see the following containers up:

| Container | Port |
|---|---|
| Nginx (reverse proxy) | 80 |
| Spring Boot backend | 8080 |
| React frontend | — |
| MySQL | 3306 |

### Step 5 — Check Backend Logs (Optional)

```bash
docker compose logs -f backend-app
```

Watch until you see Tomcat start on port 8080 and Flyway confirm database migrations are complete. Press `Ctrl + C` to exit.

### Stopping the Stack

```bash
docker compose down
```

> ⚠️ **Important:** Never run `docker compose down -v` unless you intentionally want to wipe the database. The `-v` flag deletes volumes (including all MySQL data).

### Low Memory? Add Swap(Free Tier AWS Deployement)

If your machine runs out of memory:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
free -h
```

---

## Backend Concepts Demonstrated

This project demonstrates several **real-world backend and distributed systems engineering patterns**:

- Asynchronous, non-blocking request processing
- Distributed-safe design (multi-instance, no race conditions)
- Horizontal scalability
- Idempotent validation handling
- Circuit breaker pattern (Resilience4j)
- Distributed rate limiting (Redis + Bucket4j)
- Webhook-driven architecture with HMAC security
- Scheduler-based failure recovery
- Event-driven validation lifecycle
- Secure API design (JWT + HMAC)
- API versioning
- Containerized deployment (Docker Compose)
- Cloud deployment (AWS EC2)

---

## Current Gaps & Future Improvements

### Not Yet Implemented

- HTTPS (currently HTTP due to free-tier constraints)
- Monitoring & alerting (Prometheus, Grafana)
- Distributed tracing
- Message queues (Kafka / RabbitMQ) for fully decoupled processing

### Planned Enhancements

- Introduce message queues for decoupled, resilient processing
- Add full observability stack (metrics + distributed tracing)
- Enable HTTPS with production-grade TLS
- Auto-scaling infrastructure

---

## Author

**Venkatesh**  
Backend Developer
