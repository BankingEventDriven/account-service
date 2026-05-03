package com.bank.account_service.application.service;

import com.bank.account_service.application.command.DebitAccountCommand;
import com.bank.account_service.application.command.OpenAccountCommand;
import com.bank.account_service.application.command.UpdateAccountStatusCommand;
import com.bank.account_service.domain.event.DomainEventPublisher;
import com.bank.account_service.domain.exception.AccountNotFoundException;
import com.bank.account_service.domain.exception.DuplicateTransactionException;
import com.bank.account_service.domain.model.*;
import com.bank.account_service.domain.policy.DebitPolicy;
import com.bank.account_service.domain.factory.DebitPolicyFactory;
import com.bank.account_service.domain.repository.AccountRepository;
import com.bank.account_service.infrastructure.persistence.repository.projection.AccountBalanceView;
import com.bank.account_service.infrastructure.persistence.repository.read.AccountQueryRepository;
import com.bank.account_service.interfaces.dto.AccountDetailResponse;
import com.bank.account_service.interfaces.dto.AccountResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final AccountQueryRepository queryRepository;
    private final DomainEventPublisher eventPublisher;
    private final DebitPolicyFactory debitPolicyFactory;

    @Transactional
    public AccountId openAccount(OpenAccountCommand command) {
        AccountId id = new AccountId(UUID.randomUUID());
        Owner owner = new Owner(command.firstName(), command.lastName());
        Money initialBalance = new Money(
                command.initialBalance(),
                Currency.getInstance(command.currency()));

        Account account = Account.open(id, owner, command.type(), initialBalance);
        repository.save(account);

        // Domain events (e.g. AccountOpened) would be pulled and published here.
        // Omitted for brevity — same pattern as handleDebit().
        return id;
    }

    public Optional<AccountDetailResponse> findById(UUID accountId) {
        return repository.findById(new AccountId(accountId))
            .map(account -> new AccountDetailResponse(
                    account.getId().value(),
                    account.getOwner().firstName(),
                    account.getOwner().lastName(),
                    account.getType(),
                    account.getStatus(),
                    account.getBalance().getAmount(),
                    account.getBalance().getCurrency().getCurrencyCode()
            ));
    }

    /**
     * Debits an account.
     *
     * FIX compared to original:
     *   1. Idempotency check added — 409 if transactionId already processed.
     *   2. Events published AFTER repository.save() inside the same @Transactional boundary.
     *      If Pulsar publish fails here the transaction rolls back, keeping DB and events
     *      consistent. For production prefer the Outbox Pattern.
     *   3. Uses AccountNotFoundException (404) instead of RuntimeException (500).
     */
    @Transactional
    public void handleDebit(DebitAccountCommand command) {
        // Idempotency guard — prevents double-debit on retried requests.
        if (repository.existsByTransactionIdProcessed(command.transactionId())) {
            throw new DuplicateTransactionException(command.transactionId());
        }

        Account account = repository.findById(new AccountId(command.accountId()))
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        DebitPolicy policy = debitPolicyFactory.get(account.getType());

        account.debit(
                new Money(command.amount(), Currency.getInstance("EUR")),
                command.transactionId(),
                policy);

        repository.save(account);

        // Pull events after save — if save throws, events are never published.
        account.pullEvents().forEach(eventPublisher::publish);
    }

    @Transactional
    public void updateAccountStatus(UpdateAccountStatusCommand command) {
        Account account = repository.findById(new AccountId(command.accountId()))
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.changeStatus(command.newStatus());
        repository.save(account);
        account.pullEvents().forEach(eventPublisher::publish);
    }

    public Page<AccountResponse> findAll(Pageable pageable) {
        return queryRepository.findAll(pageable)
            .map(account -> new AccountResponse(
                    account.getId().value(),
                    account.getBalance().getAmount(),
                    account.getBalance().getCurrency().getCurrencyCode(),
                    account.getStatus().name()
            ));
    }

    public List<AccountBalanceView> findBalances() {
        return queryRepository.findBalances();
    }
}
