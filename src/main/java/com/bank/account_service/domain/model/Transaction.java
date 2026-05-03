package com.bank.account_service.domain.model;

import com.bank.account_service.domain.model.enums.TransactionType;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Transaction {
    private UUID id;
    private AccountId accountId;
    private Money amount;
    private TransactionType type;
    private Instant createdAt;

    private Transaction(UUID id, AccountId accountId, Money amount, TransactionType type, Instant createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static Transaction restore(UUID id, AccountId accountId, Money amount, TransactionType type, Instant createdAt) {
        return new Transaction(id, accountId, amount, type, createdAt);
    }

    public static Transaction debit(AccountId accountId, Money amount) {
        return new Transaction(
            UUID.randomUUID(),
            accountId,
            amount,
            TransactionType.DEBIT,
            Instant.now()
        );
    }
}
