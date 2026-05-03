package com.bank.account_service.domain.event;

import com.bank.account_service.domain.model.AccountId;
import com.bank.account_service.domain.model.enums.AccountStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when an account transitions between statuses (e.g. ACTIVE → BLOCKED).
 */
public record AccountStatusChanged(
        UUID eventId,
        AccountId accountId,
        AccountStatus previousStatus,
        AccountStatus newStatus,
        Instant occurredAt
) implements DomainEvent {

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
