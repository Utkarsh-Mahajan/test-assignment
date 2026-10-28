package com.example.ledger.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionType {
    CREDIT,
    DEBIT;

    @JsonCreator
    public static TransactionType from(String value) {
        try {
            return value == null ? null : TransactionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Transaction type must be credit or debit");
        }
    }
}
