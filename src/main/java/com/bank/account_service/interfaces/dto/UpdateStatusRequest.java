package com.bank.account_service.interfaces.dto;

import com.bank.account_service.domain.model.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for partial update of an account's status.
 *
 * Spring MVC interview topic: PATCH vs PUT
 *   PUT    — replaces the entire resource. If a field is missing in the body, it's set to null.
 *   PATCH  — partial update. Only the fields provided in the body are changed.
 *            Not guaranteed idempotent: e.g. PATCH "increment counter" applied twice ≠ once.
 *            Changing status ACTIVE→BLOCKED IS idempotent (same result if repeated).
 *
 * This DTO carries only the new status — not the whole account — which is intentional for PATCH.
 */
public record UpdateStatusRequest(
    @NotNull(message = "Status is required")
    AccountStatus status
) {}
