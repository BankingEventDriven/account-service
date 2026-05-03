package com.bank.account_service.application.command;

import com.bank.account_service.domain.model.enums.AccountType;

import java.math.BigDecimal;

public record OpenAccountCommand(
    String firstName,
    String lastName,
    AccountType type,
    BigDecimal initialBalance,
    String currency
) {
}
