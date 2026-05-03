package com.bank.account_service.infrastructure.persistence.repository;

import com.bank.account_service.domain.model.Account;
import com.bank.account_service.domain.model.AccountId;
import com.bank.account_service.domain.repository.AccountRepository;
import com.bank.account_service.infrastructure.persistence.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final SpringDataAccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public Optional<Account> findById(AccountId id) {
        return accountRepository.findById(id.value())
                .map(accountMapper::toDomain);
    }

    @Override
    public List<Account> findAll() {

        return accountRepository.findAll().stream()
                .map(accountMapper::toDomain).toList();
    }

    @Override
    public void save(Account account) {
        accountRepository.save(accountMapper.toEntity(account));
    }

    @Override
    public boolean existsById(AccountId id) {
        return accountRepository.existsById(id.value());
    }

    @Override
    public boolean existsByTransactionIdProcessed(UUID transactionId) {
        return false;
    }
}
