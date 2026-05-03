package com.bank.account_service.interfaces.dto;

import com.bank.account_service.domain.model.enums.AccountStatus;
import com.bank.account_service.domain.model.enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDetailResponse(
    UUID accountId,
    String firstName,
    String lastName,
    AccountType type,
    AccountStatus status,
    BigDecimal balance,
    String currency
) {
}
