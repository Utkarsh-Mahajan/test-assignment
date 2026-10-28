package com.example.ledger.api;

import com.example.ledger.domain.LedgerTransaction;
import com.example.ledger.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public final class Dtos {
    private Dtos() {}

    public record AccountResponse(Long id, String name, BigDecimal balance) {}

    public record TransactionResponse(Long id, Long accountId, TransactionType type, BigDecimal amount,
                                      String referenceId, String description, Instant createdAt) {
        public static TransactionResponse from(LedgerTransaction transaction) {
            return new TransactionResponse(transaction.getId(), transaction.getAccount().getId(),
                    transaction.getType(), transaction.getAmount(), transaction.getReferenceId(),
                    transaction.getDescription(), transaction.getCreatedAt());
        }
    }

    public record PageResponse<T>(java.util.List<T> content, int page, int limit,
                                  long totalRecords, int totalPages) {}

    public record TransferResponse(TransactionResponse debit, TransactionResponse credit) {}
}
