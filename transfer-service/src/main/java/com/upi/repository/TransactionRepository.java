package com.upi.repository;

import com.upi.entity.Transaction;
import com.upi.entity.Transaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Repository - Spring Data JPA Repository
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Find transaction by reference number
     */
    Optional<Transaction> findByTransactionRef(String transactionRef);

    /**
     * Find all transactions by payer VPA
     */
    List<Transaction> findByPayerVpa(String payerVpa);

    /**
     * Find all transactions by payee VPA
     */
    List<Transaction> findByPayeeVpa(String payeeVpa);

    /**
     * Find transactions by status
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Find transactions within date range
     */
    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find transactions by payer VPA within date range
     */
    List<Transaction> findByPayerVpaAndCreatedAtBetween(
            String payerVpa, LocalDateTime start, LocalDateTime end);

    /**
     * Get daily transaction total for a payer
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.payerVpa = :payerVpa " +
           "AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startOfDay AND :endOfDay")
    BigDecimal getDailyTransactionTotal(
            @Param("payerVpa") String payerVpa,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Count daily transactions for a payer
     */
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.payerVpa = :payerVpa " +
           "AND t.status = 'SUCCESS' " +
           "AND t.createdAt BETWEEN :startOfDay AND :endOfDay")
    int getDailyTransactionCount(
            @Param("payerVpa") String payerVpa,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Find pending transactions older than specified time
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.status IN ('PENDING', 'PROCESSING') " +
           "AND t.createdAt < :threshold")
    List<Transaction> findStaleTransactions(@Param("threshold") LocalDateTime threshold);

    /**
     * Find recent transactions for a payer (limited)
     */
    List<Transaction> findTop10ByPayerVpaOrderByCreatedAtDesc(String payerVpa);

    /**
     * Check if transaction reference exists
     */
    boolean existsByTransactionRef(String transactionRef);

    /**
     * Count transactions by status
     */
    long countByStatus(TransactionStatus status);

    /**
     * Find transactions by type and status
     */
    List<Transaction> findByTransactionTypeAndStatus(String transactionType, TransactionStatus status);
}
