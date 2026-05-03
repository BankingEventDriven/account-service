package com.bank.account_service.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Business type of account",
    example = "CURRENT"
)
public enum AccountType {
    CURRENT,
    SAVINGS,
    CREDIT
}
