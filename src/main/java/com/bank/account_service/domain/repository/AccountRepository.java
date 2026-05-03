package com.bank.account_service.domain.repository;

import com.bank.account_service.domain.model.Account;
import com.bank.account_service.domain.model.AccountId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<Account> findById(AccountId id);
    List<Account> findAll();
    void save(Account account);
    boolean existsById(AccountId id);
    boolean existsByTransactionIdProcessed(UUID transactionId);
}
