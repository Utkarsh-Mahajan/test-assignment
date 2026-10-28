package com.example.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Check(constraints = "amount > 0")
@Table(name = "transactions", uniqueConstraints = @UniqueConstraint(
        name = "uk_account_reference", columnNames = {"account_id", "reference_id"}),
        indexes = @Index(name = "idx_transaction_created", columnList = "created_at DESC"))
public class LedgerTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "reference_id", nullable = false, updatable = false)
    private String referenceId;

    @Column(nullable = false, updatable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerTransaction() {}

    public LedgerTransaction(Account account, TransactionType type, BigDecimal amount,
                             String referenceId, String description) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
        this.description = description;
    }

    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getReferenceId() { return referenceId; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
