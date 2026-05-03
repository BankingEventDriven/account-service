package com.bank.account_service.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitRequest(
    @NotNull UUID transactionId,
    @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
