package com.bank.account_service.domain.event;

import com.bank.account_service.domain.model.AccountId;
import com.bank.account_service.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a debit is rejected due to insufficient funds.
 *
 * BUG FIX: the original implementation overrode getEventId() and getOccurredAt()
 * returning null instead of delegating to the record components. Fixed below.
 */
public record InsufficientFundsRejected(
        UUID eventId,
        AccountId accountId,
        Money requestedAmount,
        UUID transactionId,
        Instant occurredAt
) implements DomainEvent {

    // Delegate to record components — do NOT return null.
    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}