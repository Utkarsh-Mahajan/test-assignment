package com.example.ledger.config;

import com.example.ledger.domain.Account;
import com.example.ledger.domain.LedgerTransaction;
import com.example.ledger.domain.TransactionType;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.repository.TransactionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProperty(name = "ledger.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public DemoDataSeeder(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (accounts.count() != 0 || transactions.count() != 0) {
            return;
        }

        Account primary = new Account("Avery Morgan - Primary Checking");
        Account savings = new Account("Avery Morgan - Savings");
        Account travel = new Account("Jordan Lee - Travel Fund");
        accounts.saveAll(List.of(primary, savings, travel));

        seed(primary, TransactionType.CREDIT, "4750.00", "demo-payroll-001", "Monthly payroll");
        seed(primary, TransactionType.DEBIT, "550.00", "demo-rent-001", "Apartment rent");
        seed(savings, TransactionType.CREDIT, "18500.00", "demo-transfer-001", "Emergency fund transfer");
        seed(travel, TransactionType.CREDIT, "3200.00", "demo-deposit-001", "Travel fund deposit");

        accounts.saveAll(List.of(primary, savings, travel));
    }

    private void seed(Account account, TransactionType type, String amount, String referenceId,
                      String description) {
        account.apply(type, new BigDecimal(amount));
        transactions.save(new LedgerTransaction(account, type, new BigDecimal(amount),
                referenceId, description));
    }
}
