# AGENTS.md

## Project Overview
`account-service` owns account state and balance management. It is the source of truth for account-level invariants such as available funds, debit/credit application, and account lifecycle rules.

Its role in the system is to protect the account bounded context while reacting to transfer-related events from `transaction-service`.

## Responsibilities
- Own account aggregates, balances, reservation/debit/credit rules, and account persistence.
- Validate whether a balance change is allowed according to account rules.
- Publish account-side outcomes needed by transfer workflows.

Must NOT:
- Orchestrate end-to-end transfers across services.
- Own transaction history as a workflow domain.
- Reach into `transaction-service` tables or depend on its internal model.

## Tech Stack
- Spring Boot service
- PostgreSQL for account persistence
- Apache Pulsar for asynchronous event consumption/publication
- Docker for local packaging and compose-based startup

## Architecture Notes
Use a layered structure under `src/main/java/com/bank/account_service`:
- `domain`: account aggregate, balance policies, value objects, domain services
- `application`: use cases for applying debits/credits and handling commands/events
- `infrastructure`: JPA repositories, Pulsar adapters, database config, external serializers
- `interfaces`: REST endpoints if needed, event listeners, admin/read APIs

DDD boundary:
- This repo owns the Account bounded context.
- Only account-related concepts belong here; transaction workflow state does not.

## Communication
Produces:
- Account outcome events that support transfer completion/failure
- In the current system vocabulary, this service should contribute to `TransactionCompleted` or `TransactionFailed` by reporting whether balance updates succeeded

Consumes:
- `TransactionInitiated`

Interacts with:
- `transaction-service` via Apache Pulsar events
- `shared-contracts` for event payload classes/schemas
- PostgreSQL for its own account store only

## Run Instructions
Local:
- `mvn spring-boot:run`
- Default port: `8080`

Container:
- Build with Maven/Jib or a future Dockerfile, then run from `infrastructure/docker-compose.yml`

Environment dependencies:
- PostgreSQL connection settings
- Pulsar broker URL
- Any schema migration tool config if introduced later

## Coding Guidelines
- Keep balance rules and account invariants in the domain layer.
- Event listeners should hand off quickly to application services.
- Persist only account-owned data in this repo.
- Use events for coordination with other services; do not add cross-service DB access.

## Constraints
- Do NOT embed transfer orchestration logic here.
- Do NOT duplicate transaction workflow state from `transaction-service`.
- Do NOT expose account internals as shared mutable data for other services.
- Respect the Account bounded context even if the database is shared locally for convenience.

## Folder Structure Explanation
- `src/main/java/com/bank/account_service/`: service code root; create `domain`, `application`, `infrastructure`, and `interfaces` packages under it
- `src/main/resources/application.yaml`: service config, datasource settings, Pulsar config
- `src/test/java/`: unit tests for domain rules and integration tests for persistence/event handlers
- `pom.xml`: Maven build and dependencies
- `domain-java-objects.txt`: exported snapshot of all Java sources under the `domain` package
- `application-java-objects.txt`: exported snapshot of all Java sources under the `application` package
- `infrastructure-java-objects.txt`: exported snapshot of all Java sources under the `infrastructure` package
- `interface-java-objects.txt`: exported snapshot of all Java sources under the `interfaces` package

Current state:
- The layered packages are now present under `src/main/java/com/bank/account_service/`, including `application`, `domain`, `infrastructure`, and `interfaces`.
- `AccountServiceApplication` should remain a bootstrap entry point only; keep business logic in the layered packages above.
- The exported `*-java-objects.txt` files should be refreshed whenever their corresponding package source changes and are intended as documentation snapshots, not source-of-truth files.
