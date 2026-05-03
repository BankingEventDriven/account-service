package com.bank.account_service.infrastructure.persistence.entity;

import com.bank.account_service.domain.model.enums.TransactionType;
import com.bank.account_service.infrastructure.persistence.embeddable.MoneyEmbeddable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Embedded
    private MoneyEmbeddable amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;
}
