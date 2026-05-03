package com.bank.account_service.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Type of transaction applied to an account",
    example = "DEBIT"
)
public enum TransactionType {
    DEBIT,
    CREDIT,
    TRANSFER
}
