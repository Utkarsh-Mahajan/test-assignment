package com.example.ledger.api;

import com.example.ledger.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull(message = "Account ID is required") Long accountId,
        @NotNull(message = "Transaction type is required") TransactionType type,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,
        @NotBlank(message = "Reference ID is required") String referenceId,
        String description
) {
}
