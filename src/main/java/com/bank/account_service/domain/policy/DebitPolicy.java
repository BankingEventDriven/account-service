package com.bank.account_service.domain.policy;

import com.bank.account_service.domain.model.Account;
import com.bank.account_service.domain.model.Money;

public interface DebitPolicy {
    void validate(Account account, Money amount);
}
