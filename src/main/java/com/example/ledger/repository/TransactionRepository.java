package com.example.ledger.repository;

import com.example.ledger.domain.LedgerTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<LedgerTransaction, Long> {
    @EntityGraph(attributePaths = "account")
    Page<LedgerTransaction> findAllBy(Pageable pageable);

    boolean existsByAccountIdAndReferenceId(Long accountId, String referenceId);

    @Query("select case when count(t) > 0 then true else false end from LedgerTransaction t "
            + "where t.account.id = :accountId and t.referenceId = :referenceId")
    boolean referencedOnAccount(@Param("accountId") Long accountId,
                                @Param("referenceId") String referenceId);
}
