package com.bank.account_service.application.command;

import com.bank.account_service.domain.model.enums.AccountStatus;

import java.util.UUID;

/**
 * Command to change the status of an account (ACTIVE → BLOCKED, BLOCKED → ACTIVE, etc.).
 * Triggered by PATCH /api/v1/accounts/{id}/status.
 */
public record UpdateAccountStatusCommand(
    UUID accountId,
    AccountStatus newStatus
) {}