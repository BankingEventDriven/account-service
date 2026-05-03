package com.bank.account_service.domain.exception;

import java.util.UUID;


/**
 * Thrown when a debit command carries a transactionId that was already processed.
 * Implements idempotency protection at the domain level.
 * Maps to HTTP 409 Conflict in GlobalExceptionHandler.
 */
public class DuplicateTransactionException extends RuntimeException {

    private final UUID transactionId;

    public DuplicateTransactionException(UUID transactionId) {
        super("Transaction already processed: " + transactionId);
        this.transactionId = transactionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
}