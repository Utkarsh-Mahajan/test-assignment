package com.example.ledger.domain;

import com.example.ledger.exception.InsufficientBalanceException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Check(constraints = "balance >= 0")
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected Account() {}

    public Account(String name) {
        this.name = name;
        this.balance = BigDecimal.ZERO.setScale(2);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }

    public void apply(TransactionType type, BigDecimal amount) {
        if (type == TransactionType.CREDIT) {
            balance = balance.add(amount);
        } else {
            if (balance.compareTo(amount) < 0) {
                throw new InsufficientBalanceException();
            }
            balance = balance.subtract(amount);
        }
    }
}
