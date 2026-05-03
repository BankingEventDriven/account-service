package com.bank.account_service.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Lifecycle status of an account",
    example = "ACTIVE"
)
public enum AccountStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED,
    CLOSED
}
