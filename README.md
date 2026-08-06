# PaymentSystem

A banking payment system backend built with **Java 21** and **Spring Boot 3.2.5**, simulating core banking operations such as account management, card issuance, transfers, and payment processing.

## Tech Stack

- **Java 21**, **Spring Boot 3.2.5**
- **Spring Web** — REST API layer
- **Spring Data JPA** + **Hibernate** — persistence layer
- **PostgreSQL** — relational database
- **Liquibase** — database migrations
- **Spring Security + JWT** (jjwt) — authentication and authorization
- **ShedLock** — prevents duplicate scheduled job execution across instances
- **Spring Cloud OpenFeign** — integration with the Central Bank of Azerbaijan (CBAR) currency rates API
- **Springdoc OpenAPI (Swagger)** — API documentation
- **JUnit 5 + Mockito** — unit testing
- **i18n** — localized messages (EN/RU/AZ)

## Features

- Customer registration and JWT-based authentication
- Customer profile management
- **Current accounts**: opening, deposits, cancellation, expiration handling, per-customer limits
- **Cards**: ordering, deposits, blocking/unblocking, cancellation, expiration handling
- **Transfers**: card-to-card, card-to-account, account-to-account, and external transfers with commission calculation
- **Transactions**: full operation history
- **Orders**: application/approval flow for opening new accounts and cards
- **Scheduled jobs**: automatic expiration of cards/accounts, scheduled payment processing
- Business validation: customer status, age restrictions, operation limits
- Centralized exception handling with localized error messages

## Architecture

Layered architecture: **Controller → Service → Repository**, with separate DTOs (`model` package) decoupled from JPA entities, custom exceptions per business case, and a `SecurityUtils` component enforcing that customers can only access their own resources.

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL running locally
- Environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_PASSWORD`

### Run

```bash
./gradlew bootRun
```

The application starts on `http://localhost:8089`.

### API Documentation

Swagger UI is available at `http://localhost:8089/swagger-ui.html`.
Most endpoints require a JWT Bearer token — obtain one via the `/auth/login` endpoint and authorize using the "Authorize" button in Swagger UI.