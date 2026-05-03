package com.bank.account_service.interfaces.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
    UUID accountId,
    BigDecimal balance,
    String currency,
    String status
) {
}
