package com.bank.account_service.infrastructure.persistence.repository.read;

import com.bank.account_service.domain.model.Account;
import com.bank.account_service.domain.model.Transaction;
import com.bank.account_service.infrastructure.persistence.repository.projection.AccountBalanceView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface AccountQueryRepository {

    Page<Account> findAll(Pageable pageable);
    List<AccountBalanceView> findBalances();
    List<Transaction> findByAccountId(UUID accountId);
}
