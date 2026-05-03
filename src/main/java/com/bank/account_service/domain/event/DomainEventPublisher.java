package com.bank.account_service.domain.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
