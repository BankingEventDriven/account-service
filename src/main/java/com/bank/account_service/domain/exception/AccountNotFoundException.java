package com.bank.account_service.domain.exception;

import java.util.UUID;

/**
 * Thrown when an account lookup by ID finds no result.
 * Maps to HTTP 404 Not Found in GlobalExceptionHandler.
 */
public class AccountNotFoundException extends RuntimeException {

    private final UUID accountId;

    public AccountNotFoundException(UUID accountId) {
        super("Account not found: " + accountId);
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
