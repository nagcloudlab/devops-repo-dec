package com.upi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Entity - JPA Entity for UPI Transactions
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_ref", columnList = "transactionRef"),
    @Index(name = "idx_payer_vpa", columnList = "payerVpa"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 30)
    private String transactionRef;

    @Column(nullable = false, length = 100)
    private String payerVpa;

    @Column(nullable = false, length = 100)
    private String payeeVpa;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 10, scale = 2)
    private BigDecimal charges;

    @Column(length = 20)
    private String transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(length = 500)
    private String remarks;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @Column(length = 50)
    private String bankRrn;

    @Version
    private Long version;

    // Enum for transaction status
    public enum TransactionStatus {
        INITIATED,
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        REVERSED,
        TIMEOUT,
        CANCELLED
    }

    // Default constructor
    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.status = TransactionStatus.INITIATED;
    }

    // Builder pattern
    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getPayerVpa() { return payerVpa; }
    public void setPayerVpa(String payerVpa) { this.payerVpa = payerVpa; }

    public String getPayeeVpa() { return payeeVpa; }
    public void setPayeeVpa(String payeeVpa) { this.payeeVpa = payeeVpa; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getCharges() { return charges; }
    public void setCharges(BigDecimal charges) { this.charges = charges; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getBankRrn() { return bankRrn; }
    public void setBankRrn(String bankRrn) { this.bankRrn = bankRrn; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public long getProcessingTimeMs() {
        if (createdAt != null && completedAt != null) {
            return java.time.Duration.between(createdAt, completedAt).toMillis();
        }
        return -1;
    }

    // Builder class
    public static class TransactionBuilder {
        private final Transaction txn = new Transaction();

        public TransactionBuilder transactionRef(String ref) {
            txn.setTransactionRef(ref);
            return this;
        }

        public TransactionBuilder payerVpa(String vpa) {
            txn.setPayerVpa(vpa);
            return this;
        }

        public TransactionBuilder payeeVpa(String vpa) {
            txn.setPayeeVpa(vpa);
            return this;
        }

        public TransactionBuilder amount(BigDecimal amount) {
            txn.setAmount(amount);
            return this;
        }

        public TransactionBuilder charges(BigDecimal charges) {
            txn.setCharges(charges);
            return this;
        }

        public TransactionBuilder transactionType(String type) {
            txn.setTransactionType(type);
            return this;
        }

        public TransactionBuilder status(TransactionStatus status) {
            txn.setStatus(status);
            return this;
        }

        public TransactionBuilder remarks(String remarks) {
            txn.setRemarks(remarks);
            return this;
        }

        public Transaction build() {
            return txn;
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", transactionRef='" + transactionRef + '\'' +
                ", payerVpa='" + payerVpa + '\'' +
                ", payeeVpa='" + payeeVpa + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}
