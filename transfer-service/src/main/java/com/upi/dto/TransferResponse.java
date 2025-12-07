package com.upi.dto;

import com.upi.entity.Transaction.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transfer Response DTO - Output for fund transfer API
 */
@Schema(description = "UPI Fund Transfer Response")
public class TransferResponse {

    @Schema(description = "Unique transaction reference", example = "TXN20241207120000AB")
    private String transactionRef;

    @Schema(description = "Transaction status", example = "SUCCESS")
    private TransactionStatus status;

    @Schema(description = "Status message", example = "Transfer completed successfully")
    private String message;

    @Schema(description = "Transaction timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Transfer amount", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Transaction charges", example = "0.00")
    private BigDecimal charges;

    @Schema(description = "Total deducted amount", example = "1000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Payer VPA", example = "user@sbi")
    private String payerVpa;

    @Schema(description = "Payee VPA", example = "merchant@hdfc")
    private String payeeVpa;

    @Schema(description = "Bank reference number")
    private String bankRrn;

    // Constructors
    public TransferResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public TransferResponse(String transactionRef, TransactionStatus status, String message) {
        this.transactionRef = transactionRef;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Builder pattern
    public static TransferResponseBuilder builder() {
        return new TransferResponseBuilder();
    }

    // Getters and Setters
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getCharges() { return charges; }
    public void setCharges(BigDecimal charges) { this.charges = charges; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPayerVpa() { return payerVpa; }
    public void setPayerVpa(String payerVpa) { this.payerVpa = payerVpa; }

    public String getPayeeVpa() { return payeeVpa; }
    public void setPayeeVpa(String payeeVpa) { this.payeeVpa = payeeVpa; }

    public String getBankRrn() { return bankRrn; }
    public void setBankRrn(String bankRrn) { this.bankRrn = bankRrn; }

    // Builder class
    public static class TransferResponseBuilder {
        private final TransferResponse response = new TransferResponse();

        public TransferResponseBuilder transactionRef(String ref) {
            response.setTransactionRef(ref);
            return this;
        }

        public TransferResponseBuilder status(TransactionStatus status) {
            response.setStatus(status);
            return this;
        }

        public TransferResponseBuilder message(String message) {
            response.setMessage(message);
            return this;
        }

        public TransferResponseBuilder amount(BigDecimal amount) {
            response.setAmount(amount);
            return this;
        }

        public TransferResponseBuilder charges(BigDecimal charges) {
            response.setCharges(charges);
            return this;
        }

        public TransferResponseBuilder totalAmount(BigDecimal totalAmount) {
            response.setTotalAmount(totalAmount);
            return this;
        }

        public TransferResponseBuilder payerVpa(String payerVpa) {
            response.setPayerVpa(payerVpa);
            return this;
        }

        public TransferResponseBuilder payeeVpa(String payeeVpa) {
            response.setPayeeVpa(payeeVpa);
            return this;
        }

        public TransferResponseBuilder bankRrn(String bankRrn) {
            response.setBankRrn(bankRrn);
            return this;
        }

        public TransferResponse build() {
            return response;
        }
    }
}
