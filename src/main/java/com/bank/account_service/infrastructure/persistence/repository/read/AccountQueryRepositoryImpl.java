package com.bank.account_service.infrastructure.persistence.repository.read;

import com.bank.account_service.domain.model.Account;
import com.bank.account_service.domain.model.Transaction;
import com.bank.account_service.infrastructure.persistence.mapper.AccountMapper;
import com.bank.account_service.infrastructure.persistence.mapper.TransactionMapper;
import com.bank.account_service.infrastructure.persistence.repository.SpringDataAccountRepository;
import com.bank.account_service.infrastructure.persistence.repository.projection.AccountBalanceView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountQueryRepositoryImpl implements AccountQueryRepository {

    private final SpringDataAccountRepository repository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    @Override
    public Page<Account> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(accountMapper::toDomain);
    }

    @Override
    public List<AccountBalanceView> findBalances() {
        return repository.findBalances();
    }

    @Override
    public List<Transaction> findByAccountId(UUID accountId) {
        return repository.findByAccountId(accountId)
            .stream()
            .map(transactionMapper::toDomain)
            .toList();
    }
}
