package com.example.ledger.service;

import com.example.ledger.api.Dtos.AccountResponse;
import com.example.ledger.api.Dtos.PageResponse;
import com.example.ledger.api.Dtos.TransferResponse;
import com.example.ledger.api.Dtos.TransactionResponse;
import com.example.ledger.api.CreateAccountRequest;
import com.example.ledger.api.CreateTransactionRequest;
import com.example.ledger.api.TransferRequest;
import com.example.ledger.domain.Account;
import com.example.ledger.domain.LedgerTransaction;
import com.example.ledger.exception.DuplicateTransactionException;
import com.example.ledger.exception.InsufficientBalanceException;
import com.example.ledger.exception.ResourceNotFoundException;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LedgerService {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public LedgerService(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = accounts.save(new Account(request.name().trim()));
        return new AccountResponse(account.getId(), account.getName(), account.getBalance());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id) {
        Account account = accounts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
        return new AccountResponse(account.getId(), account.getName(), account.getBalance());
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        Account account = accounts.findByIdForUpdate(request.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.accountId()));
        ensureUnique(account.getId(), request.referenceId());
        account.apply(request.type(), request.amount());
        String description = request.description() == null ? "" : request.description().trim();
        LedgerTransaction transaction = transactions.save(
                new LedgerTransaction(account, request.type(), request.amount(),
                        request.referenceId().trim(), description));
        return TransactionResponse.from(transaction);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactions(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<LedgerTransaction> result = transactions.findAllBy(pageable);
        List<TransactionResponse> content = result.getContent().stream()
                .map(TransactionResponse::from).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new IllegalArgumentException("Source and destination accounts must differ");
        }
        Long firstId = Math.min(request.sourceAccountId(), request.destinationAccountId());
        Long secondId = Math.max(request.sourceAccountId(), request.destinationAccountId());
        Account first = accounts.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", firstId));
        Account second = accounts.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", secondId));
        Account source = first.getId().equals(request.sourceAccountId()) ? first : second;
        Account destination = first.getId().equals(request.destinationAccountId()) ? first : second;

        ensureUnique(source.getId(), request.referenceId());
        ensureUnique(destination.getId(), request.referenceId());
        source.apply(com.example.ledger.domain.TransactionType.DEBIT, request.amount());
        destination.apply(com.example.ledger.domain.TransactionType.CREDIT, request.amount());

        LedgerTransaction debit = transactions.save(new LedgerTransaction(source,
                com.example.ledger.domain.TransactionType.DEBIT, request.amount(),
                request.referenceId().trim(), "Transfer to account " + destination.getId()));
        LedgerTransaction credit = transactions.save(new LedgerTransaction(destination,
                com.example.ledger.domain.TransactionType.CREDIT, request.amount(),
                request.referenceId().trim(), "Transfer from account " + source.getId()));
        return new TransferResponse(TransactionResponse.from(debit),
                TransactionResponse.from(credit));
    }

    private void ensureUnique(Long accountId, String referenceId) {
        if (transactions.existsByAccountIdAndReferenceId(accountId, referenceId.trim())) {
            throw new DuplicateTransactionException(referenceId);
        }
    }
}
