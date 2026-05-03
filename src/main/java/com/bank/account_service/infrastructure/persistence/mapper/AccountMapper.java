package com.bank.account_service.infrastructure.persistence.mapper;

import com.bank.account_service.domain.model.*;
import com.bank.account_service.infrastructure.persistence.entity.AccountEntity;
import com.bank.account_service.infrastructure.persistence.embeddable.MoneyEmbeddable;
import com.bank.account_service.infrastructure.persistence.embeddable.OwnerEmbeddable;
import org.springframework.stereotype.Component;
import java.util.Currency;

@Component
public class AccountMapper {

    public Account toDomain(AccountEntity entity) {
        Currency currency = Currency.getInstance(entity.getBalance().getCurrency());

        return Account.restore(
            new AccountId(entity.getId()),
            new Owner(entity.getOwner().getFirstName(), entity.getOwner().getLastName()),
            entity.getStatus(),
            new Money(entity.getBalance().getAmount(), currency),
            entity.getType(),
            entity.getVersion()
        );
    }

    public AccountEntity toEntity(Account domain) {
        return new AccountEntity(
            domain.getId().value(),
            new OwnerEmbeddable(
                domain.getOwner().firstName(),
                domain.getOwner().lastName()
            ),
            domain.getStatus(),
            domain.getType(),
            new MoneyEmbeddable(
                domain.getBalance().getAmount(),
                domain.getBalance().getCurrency().getCurrencyCode()
            ),
            domain.getVersion(),
            null,   // createdAt — managed by @CreatedDate (set on first insert, never overwritten)
            null   // updatedAt — managed by @LastModifiedDate (updated automatically on each save)
        );
    }
}
