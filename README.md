# account-service

`account-service` owns account state, balance updates, and account-level business rules. In this event-driven banking system, it is responsible for deciding whether money can be debited or credited and for publishing account-related outcomes to the rest of the platform.

## How the Account Service Works

At a high level, the service follows a layered architecture:

- `interfaces`: receives HTTP requests and maps them into application commands
- `application`: coordinates the use case and transaction boundary
- `domain`: enforces account rules and produces domain events
- `infrastructure`: persists account data and publishes events to Apache Pulsar

## End-to-End Flow Overview

Imagine the following request flow from start to finish:

1. A client such as Postman or a frontend sends a `POST` request with a JSON body to the account API.
2. `AccountController` in the `interfaces` layer receives the request, maps the payload into a `DebitRequest`, and creates a `DebitAccountCommand`.
3. `AccountApplicationService` in the `application` layer starts the use case inside a transaction and asks `AccountRepository` to load the target account.
4. The `Account` aggregate in the `domain` layer applies the business rules, such as whether the account is active, whether currencies match, and whether sufficient funds are available.
5. If the debit is valid, the domain updates the balance and produces an `AccountDebited` domain event. If the operation cannot be completed, the domain can reject it with a failure outcome such as insufficient funds.
6. The repository implementation in the `infrastructure` layer saves the updated account state to the database through JPA.
7. `PulsarEventPublisher` in the `infrastructure` layer publishes the resulting domain event to Apache Pulsar so other services can react asynchronously.
8. The controller returns an HTTP response to the caller.

## Request Example

Example debit request:

```http
POST /api/v1/accounts/{accountId}/debit
Content-Type: application/json
```

```json
{
  "amount": 150.00,
  "transactionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

## Current Code Status

The project structure already reflects the intended design:

- `AccountController` exposes the debit endpoint
- `AccountApplicationService` defines the transactional application layer entry point
- `Account` is the domain aggregate
- JPA persistence classes exist under `infrastructure.persistence`
- Pulsar publishing is implemented in `PulsarEventPublisher`

At the moment, some parts of the business flow are still scaffolded and need implementation:

- `AccountApplicationService.handleDebit(...)` is currently empty
- `Account.debit()` is currently empty
- the controller currently returns `200 OK`

So the architecture is in place, but the full debit workflow is not yet completed end to end.
