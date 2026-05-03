package com.bank.account_service.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitAccountCommand(
    UUID accountId,
    BigDecimal amount,
    UUID transactionId
) {
}
