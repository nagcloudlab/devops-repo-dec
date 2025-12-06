package com.npci.upi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UPI Transfer Response Model
 */
public class TransferResponse {
    
    private String transactionRef;  // Unique transaction reference (e.g., TXN12345678ABCD123)
    private String status;          // SUCCESS, FAILED, PENDING
    private String statusCode;      // 00=Success, U30=Timeout, etc.
    private String payerVpa;
    private String payeeVpa;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String message;         // Human-readable status message

    // Default constructor
    public TransferResponse() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String transactionRef;
        private String status;
        private String statusCode;
        private String payerVpa;
        private String payeeVpa;
        private BigDecimal amount;
        private LocalDateTime timestamp;
        private String message;

        public Builder transactionRef(String transactionRef) {
            this.transactionRef = transactionRef;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder statusCode(String statusCode) {
            this.statusCode = statusCode;
            return this;
        }

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

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public TransferResponse build() {
            TransferResponse response = new TransferResponse();
            response.transactionRef = this.transactionRef;
            response.status = this.status;
            response.statusCode = this.statusCode;
            response.payerVpa = this.payerVpa;
            response.payeeVpa = this.payeeVpa;
            response.amount = this.amount;
            response.timestamp = this.timestamp;
            response.message = this.message;
            return response;
        }
    }

    // Getters
    public String getTransactionRef() { return transactionRef; }
    public String getStatus() { return status; }
    public String getStatusCode() { return statusCode; }
    public String getPayerVpa() { return payerVpa; }
    public String getPayeeVpa() { return payeeVpa; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }

    // Setters
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public void setStatus(String status) { this.status = status; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public void setPayerVpa(String payerVpa) { this.payerVpa = payerVpa; }
    public void setPayeeVpa(String payeeVpa) { this.payeeVpa = payeeVpa; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "TransferResponse{" +
                "transactionRef='" + transactionRef + '\'' +
                ", status='" + status + '\'' +
                ", statusCode='" + statusCode + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
