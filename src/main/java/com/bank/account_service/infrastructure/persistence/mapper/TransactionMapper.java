package com.bank.account_service.infrastructure.persistence.mapper;

import com.bank.account_service.domain.model.AccountId;
import com.bank.account_service.domain.model.Money;
import com.bank.account_service.domain.model.Transaction;
import com.bank.account_service.infrastructure.persistence.embeddable.MoneyEmbeddable;
import com.bank.account_service.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class TransactionMapper {

    public Transaction toDomain(TransactionEntity entity) {
        Currency currency = Currency.getInstance(entity.getAmount().getCurrency());

        return Transaction.restore(
            entity.getId(),
            new AccountId(entity.getAccountId()),
            new Money(entity.getAmount().getAmount(), currency),
            entity.getType(),
            entity.getCreatedAt()
        );
    }

    public TransactionEntity toEntity(Transaction domain) {
        return new TransactionEntity(
            domain.getId(),
            domain.getAccountId().value(),
            new MoneyEmbeddable(
                domain.getAmount().getAmount(),
                domain.getAmount().getCurrency().getCurrencyCode()
            ),
            domain.getType(),
            domain.getCreatedAt()
        );
    }
}
