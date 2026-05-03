package com.bank.account_service.infrastructure.persistence.repository;

import com.bank.account_service.infrastructure.persistence.entity.AccountEntity;
import com.bank.account_service.infrastructure.persistence.entity.TransactionEntity;
import com.bank.account_service.infrastructure.persistence.repository.projection.AccountBalanceView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findById(UUID id);

    @Query("""
    SELECT a.id as id, a.balance.amount as balanceAmount
    FROM AccountEntity a
    """)
    List<AccountBalanceView> findBalances();

    @Query("""
    SELECT t FROM TransactionEntity t
    WHERE t.accountId = :accountId
    ORDER BY t.createdAt DESC
    """)
    List<TransactionEntity> findByAccountId(UUID accountId);
}
