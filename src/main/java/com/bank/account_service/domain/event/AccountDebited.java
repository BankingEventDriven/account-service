package com.bank.account_service.domain.event;

import com.bank.account_service.domain.model.AccountId;
import com.bank.account_service.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

public record AccountDebited(
    UUID eventId,
    AccountId accountId,
    Money amount,
    UUID transactionId,
    Instant occurredAt
) implements DomainEvent {

    @Override public UUID getEventId() { return eventId; }
    @Override public Instant getOccurredAt() { return occurredAt; }
}
