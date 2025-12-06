package com.npci.upi.model;

import java.math.BigDecimal;

/**
 * UPI Transfer Request Model
 */
public class TransferRequest {
    
    private String payerVpa;      // Sender's VPA (e.g., rahul@okicici)
    private String payeeVpa;      // Receiver's VPA (e.g., shop@ybl)
    private BigDecimal amount;    // Transfer amount in INR
    private String remarks;       // Optional transaction note
    private String transactionType; // P2P (Person to Person) or P2M (Person to Merchant)

    // Default constructor
    public TransferRequest() {}

    // All-args constructor
    public TransferRequest(String payerVpa, String payeeVpa, BigDecimal amount, 
                          String remarks, String transactionType) {
        this.payerVpa = payerVpa;
        this.payeeVpa = payeeVpa;
        this.amount = amount;
        this.remarks = remarks;
        this.transactionType = transactionType;
    }

    // Builder pattern for cleaner test code
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String payerVpa;
        private String payeeVpa;
        private BigDecimal amount;
        private String remarks;
        private String transactionType = "P2P"; // default

        public Builder payerVpa(String payerVpa) {
            this.payerVpa = payerVpa;
            return this;
        }

        public Builder payeeVpa(String payeeVpa) {
            this.payeeVpa = payeeVpa;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder remarks(String remarks) {
            this.remarks = remarks;
            return this;
        }

        public Builder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public TransferRequest build() {
            return new TransferRequest(payerVpa, payeeVpa, amount, remarks, transactionType);
        }
    }

    // Getters and Setters
    public String getPayerVpa() { return payerVpa; }
    public void setPayerVpa(String payerVpa) { this.payerVpa = payerVpa; }

    public String getPayeeVpa() { return payeeVpa; }
    public void setPayeeVpa(String payeeVpa) { this.payeeVpa = payeeVpa; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    @Override
    public String toString() {
        return "TransferRequest{" +
                "payerVpa='" + payerVpa + '\'' +
                ", payeeVpa='" + payeeVpa + '\'' +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                '}';
    }
}
