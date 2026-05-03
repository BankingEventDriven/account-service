package com.bank.account_service.domain.policy;

import com.bank.account_service.domain.model.Account;
import com.bank.account_service.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class CurrentDebitPolicy implements DebitPolicy {

    @Override
    public void validate(Account account, Money amount) {
        if (account.getBalance().getAmount().compareTo(amount.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
    }
}
