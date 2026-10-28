package com.example.ledger.exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String referenceId) {
        super("Reference ID already exists for this account: " + referenceId);
    }
}
