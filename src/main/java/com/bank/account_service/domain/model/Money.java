package com.bank.account_service.domain.model;

import lombok.Value;

import java.math.BigDecimal;
import java.util.Currency;

@Value
public class Money {

    BigDecimal amount;
    Currency currency;

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money add(Money other) {
        if (other.amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        return new Money(amount.add(other.amount), currency);
    }
}
