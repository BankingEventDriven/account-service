package com.bank.account_service.domain.policy;

import com.bank.account_service.domain.model.Account;
import com.bank.account_service.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class SavingsDebitPolicy implements DebitPolicy {

    @Override
    public void validate(Account account, Money amount) {
        if (amount.getAmount().doubleValue() > 1000) {
            throw new IllegalStateException("Savings limit exceeded per transaction");
        }
    }
}
