package com.bank.account_service.domain.policy;

import com.bank.account_service.domain.model.Account;
import com.bank.account_service.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class CreditDebitPolicy implements DebitPolicy {

    @Override
    public void validate(Account account, Money amount) {
        // allowed to negative
    }
}
