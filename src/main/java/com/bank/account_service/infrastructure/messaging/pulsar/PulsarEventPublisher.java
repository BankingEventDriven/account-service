package com.bank.account_service.infrastructure.messaging.pulsar;

import com.bank.account_service.domain.event.DomainEvent;
import com.bank.account_service.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PulsarEventPublisher implements DomainEventPublisher {

    // Spring injectează automat acest template configurat prin YAML
    private final PulsarTemplate<DomainEvent> pulsarTemplate;

    @Override
    public void publish(DomainEvent event) {
        log.info("Publishing event: {}", event);
        pulsarTemplate.send(event);
    }
}
