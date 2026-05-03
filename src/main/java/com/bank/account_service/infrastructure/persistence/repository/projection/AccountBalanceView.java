package com.bank.account_service.infrastructure.persistence.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountBalanceView {
    UUID getId();
    BigDecimal getBalanceAmount();
}
