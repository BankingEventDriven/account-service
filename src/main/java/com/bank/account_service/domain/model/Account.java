package com.bank.account_service.domain.model;

import com.bank.account_service.domain.event.AccountDebited;
import com.bank.account_service.domain.event.AccountStatusChanged;
import com.bank.account_service.domain.event.DomainEvent;
import com.bank.account_service.domain.event.InsufficientFundsRejected;
import com.bank.account_service.domain.exception.AccountNotActiveException;
import com.bank.account_service.domain.exception.CurrencyMismatchException;
import com.bank.account_service.domain.exception.InsufficientFundsException;
import com.bank.account_service.domain.model.enums.AccountStatus;
import com.bank.account_service.domain.model.enums.AccountType;
import com.bank.account_service.domain.policy.DebitPolicy;
import lombok.Getter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Account {

    private AccountId id;
    private Owner owner;
    private AccountStatus status;
    private Money balance;
    private AccountType type;
    private int version;

    private final List<DomainEvent> events = new ArrayList<>();

    public Account(AccountId id, Owner owner, AccountStatus status, Money balance, AccountType type, int version) {
        this.id = id;
        this.owner = owner;
        this.status = status;
        this.balance = balance;
        this.type = type;
        this.version = version;
    }

    public static Account restore(AccountId id, Owner owner, AccountStatus status, Money balance, AccountType type, int version) {
        return new Account(id, owner, status, balance, type, version);
    }

    public static Account open(AccountId id, Owner owner, AccountType type, Money balance) {
        if (type == AccountType.CREDIT && balance.getAmount().signum() != 0) {
            throw new IllegalStateException("Credit accounts start with zero balance");
        }
        return new Account(id, owner, AccountStatus.ACTIVE, balance, type, 0);
    }

    /**
     * Debits this account by the given amount.
     *
     * BUG FIX (original code): guard clauses (status, currency) ran AFTER policy.validate().
     * A debit policy could execute on an INACTIVE account before the status guard fired.
     * Correct order: status → currency → policy → balance check.
     */
    public void debit(Money amount, UUID transactionId, DebitPolicy policy) {
        // 1. Status guard first — no point checking anything else on a closed account.
        if (this.status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not active: " + id.value());
        }

        // 2. Currency guard — fail fast before running any policy logic.
        if (!this.balance.getCurrency().equals(amount.getCurrency())) {
            throw new CurrencyMismatchException("Currency mismatch: expected "
                    + this.balance.getCurrency() + " but got " + amount.getCurrency());
        }

        // 3. Policy validation (type-specific rules: savings limit, credit allowed-negative, etc.)
        policy.validate(this, amount);

        // 4. Balance check — emit rejection event + throw so the caller can handle idempotency.
        if (this.balance.getAmount().compareTo(amount.getAmount()) < 0) {
            this.events.add(new InsufficientFundsRejected(
                    UUID.randomUUID(), id, amount, transactionId, Instant.now()));
            throw new InsufficientFundsException("Insufficient funds on account: " + id.value());
        }

        this.balance = new Money(
                this.balance.getAmount().subtract(amount.getAmount()),
                this.balance.getCurrency());

        this.events.add(new AccountDebited(UUID.randomUUID(), id, amount, transactionId, Instant.now()));
    }

    public void credit(Money amount, UUID transactionId) {
        this.balance = new Money(
            this.balance.getAmount().add(amount.getAmount()),
            this.balance.getCurrency()
        );
    }

    /**
     * Transitions the account to a new status.
     * Used by PATCH /api/v1/accounts/{id}/status (block, unblock, close).
     */
    public void changeStatus(AccountStatus newStatus) {
        AccountStatus previous = this.status;
        this.status = newStatus;
        this.events.add(new AccountStatusChanged(UUID.randomUUID(), id, previous, newStatus, Instant.now()));
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }
}
