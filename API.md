# API documentation

Demo data is initialized on startup. H2 remains in-memory, so this data is recreated on each restart. Demo seeding can be disabled with `--ledger.seed.enabled=false`.

## Error format

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Reference ID already exists for this account",
  "path": "/api/transactions"
}
```

Validation errors return `400` and include a `fields` object whose keys are invalid fields.

## Accounts

### Create account

`POST /api/accounts`

```json
{"name":"Primary"}
```

### Get account

`GET /api/accounts/{id}`

## Transactions

### Create transaction

`POST /api/transactions`

```json
{
  "accountId": 1,
  "type": "credit",
  "amount": 100.00,
  "referenceId": "PAY-100",
  "description": "Deposit"
}
```

`type` is `credit` or `debit`. `amount` must be positive and have at most two decimal places. `referenceId` is required and unique within the account.

### List transactions

`GET /api/transactions?page=0&limit=20`

Response:

```json
{
  "content": [],
  "page": 0,
  "limit": 20,
  "totalRecords": 0,
  "totalPages": 0
}
```

`page` is zero-based; `limit` must be from 1 to 100. Rows are latest-first.

## Transfers

### Transfer funds

`POST /api/transfers`

```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 30.00,
  "referenceId": "TRANSFER-1"
}
```

The debit and credit are committed together. Source and destination must differ and both must exist; the source must have sufficient balance.

## Status codes

- `200` successful retrieval
- `201` account, transaction, or transfer created
- `400` invalid JSON or request data
- `404` account not found
- `409` duplicate reference for an account
- `422` insufficient balance
