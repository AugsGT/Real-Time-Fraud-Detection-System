package com.frauddetection.repository;

import com.frauddetection.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountId(String accountId, Pageable pageable);

    Page<Transaction> findByStatus(Transaction.TransactionStatus status, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<Transaction> findRecentByAccount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);

    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId ORDER BY t.timestamp DESC")
    List<Transaction> findLatestByAccount(@Param("accountId") String accountId, Pageable pageable);

    long countByAccountIdAndTimestampBetween(String accountId, LocalDateTime start, LocalDateTime end);
}
