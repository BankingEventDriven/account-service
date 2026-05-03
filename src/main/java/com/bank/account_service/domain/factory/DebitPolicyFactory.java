package com.bank.account_service.domain.factory;

import com.bank.account_service.domain.model.enums.AccountType;
import com.bank.account_service.domain.policy.CreditDebitPolicy;
import com.bank.account_service.domain.policy.CurrentDebitPolicy;
import com.bank.account_service.domain.policy.DebitPolicy;
import com.bank.account_service.domain.policy.SavingsDebitPolicy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DebitPolicyFactory {

    private final Map<AccountType, DebitPolicy> policies;

    public DebitPolicyFactory() {
        this.policies = new HashMap<>();

        policies.put(AccountType.CURRENT, new CurrentDebitPolicy());
        policies.put(AccountType.CREDIT, new CreditDebitPolicy());
        policies.put(AccountType.SAVINGS, new SavingsDebitPolicy());
    }

    public DebitPolicy get(AccountType type) {
        return policies.get(type);
    }
}
