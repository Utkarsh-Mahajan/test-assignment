# Ledger API Assignment

A Spring Boot REST API that maintains account balances and immutable transactions atomically.

## Requirements

- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080/api`. The endpoint contract is documented in [API.md](API.md). The local H2 console is at `/h2-console`.

Demo data is initialized on startup. H2 remains in-memory, and the database is recreated on every application restart. This requires no external database setup, but all data is ephemeral. To disable demo seeding, start the application with `--ledger.seed.enabled=false`.

## API examples

Create an account:

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H 'Content-Type: application/json' \
  -d '{"name":"Primary"}'
```

Create a credit transaction:

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{"accountId":1,"type":"credit","amount":100.00,"referenceId":"PAY-100","description":"Deposit"}'
```

List newest transactions:

```bash
curl 'http://localhost:8080/api/transactions?page=0&limit=20'
```

Transfer funds:

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H 'Content-Type: application/json' \
  -d '{"sourceAccountId":1,"destinationAccountId":2,"amount":30.00,"referenceId":"TRANSFER-1"}'
```

## Integrity rules

- Transactions are immutable and expose no update or delete endpoint.
- `@Transactional` and pessimistic account locks make transaction creation and balance updates atomic.
- Account locks are acquired in ascending ID order for transfers, avoiding lock-order deadlocks.
- A unique `(account_id, reference_id)` constraint rejects duplicate references.
- Database checks enforce positive transaction amounts and non-negative balances.
- Monetary values use `BigDecimal` and scale to 2 decimal places.
- A failed debit or transfer has no persisted balance or transaction changes.

## Tests

```bash
mvn test
```

## Assumptions

- Page numbers are zero-based.
- Transaction listing is sorted by creation time descending, then ID descending.
- `credit` and `debit` are case-insensitive input values and are returned as uppercase enum values.
- `referenceId` must be nonblank and is unique per account.
- A transfer creates two ledger rows, one debit and one credit, using the same reference ID on different accounts.
- A transfer from an account to itself is invalid.
- Account data is intentionally not durable because H2 is in-memory.
