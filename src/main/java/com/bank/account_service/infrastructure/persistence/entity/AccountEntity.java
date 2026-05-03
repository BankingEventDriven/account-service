package com.bank.account_service.infrastructure.persistence.entity;

import com.bank.account_service.domain.model.enums.AccountStatus;
import com.bank.account_service.domain.model.enums.AccountType;
import com.bank.account_service.infrastructure.persistence.embeddable.MoneyEmbeddable;
import com.bank.account_service.infrastructure.persistence.embeddable.OwnerEmbeddable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AccountEntity {

    @Id
    private UUID id;

    @Embedded
    private OwnerEmbeddable owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Embedded
    private MoneyEmbeddable balance;

    @Version
    private int version;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
