package com.upi.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Transfer Request DTO - Input for fund transfer API
 */
@Schema(description = "UPI Fund Transfer Request")
public class TransferRequest {

    @NotBlank(message = "Payer VPA is required")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9._-]{0,49}@[a-zA-Z][a-zA-Z0-9]{1,20}$", 
             message = "Invalid Payer VPA format")
    @Schema(description = "Payer's Virtual Payment Address", example = "user@sbi", required = true)
    private String payerVpa;

    @NotBlank(message = "Payee VPA is required")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9._-]{0,49}@[a-zA-Z][a-zA-Z0-9]{1,20}$", 
             message = "Invalid Payee VPA format")
    @Schema(description = "Payee's Virtual Payment Address", example = "merchant@hdfc", required = true)
    private String payeeVpa;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum transfer amount is ₹1")
    @DecimalMax(value = "100000.00", message = "Maximum transfer amount is ₹1,00,000")
    @Digits(integer = 6, fraction = 2, message = "Invalid amount format")
    @Schema(description = "Transfer amount in INR", example = "1000.00", required = true)
    private BigDecimal amount;

    @Schema(description = "Transaction type", example = "P2P", allowableValues = {"P2P", "P2M", "BILL"})
    private String transactionType = "P2P";

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Schema(description = "Transaction remarks/note", example = "Payment for groceries")
    private String remarks;

    @Schema(description = "UPI PIN (encrypted)", example = "****")
    private String upiPin;

    // Constructors
    public TransferRequest() {}

    public TransferRequest(String payerVpa, String payeeVpa, BigDecimal amount) {
        this.payerVpa = payerVpa;
        this.payeeVpa = payeeVpa;
        this.amount = amount;
        this.transactionType = "P2P";
    }

    // Getters and Setters
    public String getPayerVpa() { return payerVpa; }
    public void setPayerVpa(String payerVpa) { this.payerVpa = payerVpa; }

    public String getPayeeVpa() { return payeeVpa; }
    public void setPayeeVpa(String payeeVpa) { this.payeeVpa = payeeVpa; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getUpiPin() { return upiPin; }
    public void setUpiPin(String upiPin) { this.upiPin = upiPin; }

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
