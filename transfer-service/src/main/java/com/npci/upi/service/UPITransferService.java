package com.npci.upi.service;

import com.npci.upi.model.TransferRequest;
import com.npci.upi.model.TransferResponse;
import com.npci.upi.exception.PaymentException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * UPI Transfer Service - Core payment processing logic
 * CI/CD Pipeline Demo
 */
public class UPITransferService {

    // UPI VPA Pattern: username@bankhandle (e.g., rahul@okicici, merchant@ybl)
    private static final Pattern VPA_PATTERN = Pattern.compile("^[a-zA-Z0-9.\\-_]{3,50}@[a-zA-Z]{3,20}$");
    
    // Transaction limits as per RBI/UPI guidelines
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000.00");
    
    // Simulated processing delay (milliseconds)
    private static final int PROCESSING_DELAY_MS = 100;

    /**
     * Validate VPA format
     * @param vpa Virtual Payment Address
     * @return true if valid format
     */
    public boolean validateVPA(String vpa) {
        if (vpa == null || vpa.trim().isEmpty()) {
            return false;
        }
        return VPA_PATTERN.matcher(vpa.trim().toLowerCase()).matches();
    }

    /**
     * Validate transfer amount against UPI limits
     * @param amount Transaction amount
     * @return true if within limits
     */
    public boolean validateAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
    }

    /**
     * Generate unique transaction reference number
     * Format: TXN + timestamp + random
     */
    public String generateTransactionRef() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN" + timestamp.substring(timestamp.length() - 8) + random;
    }

    /**
     * Process UPI Transfer - Main business logic
     * 
     * @param request Transfer request containing payer, payee, amount
     * @return TransferResponse with status and transaction details
     * @throws PaymentException if validation fails or processing error
     */
    public TransferResponse processTransfer(TransferRequest request) throws PaymentException {
        
        // Step 1: Validate request object
        if (request == null) {
            throw new PaymentException("INVALID_REQUEST", "Transfer request cannot be null");
        }

        // Step 2: Validate Payer VPA
        if (!validateVPA(request.getPayerVpa())) {
            throw new PaymentException("INVALID_PAYER_VPA", 
                "Invalid payer VPA format: " + request.getPayerVpa());
        }

        // Step 3: Validate Payee VPA
        if (!validateVPA(request.getPayeeVpa())) {
            throw new PaymentException("INVALID_PAYEE_VPA", 
                "Invalid payee VPA format: " + request.getPayeeVpa());
        }

        // Step 4: Check same account transfer
        if (request.getPayerVpa().equalsIgnoreCase(request.getPayeeVpa())) {
            throw new PaymentException("SAME_ACCOUNT", 
                "Payer and Payee cannot be the same");
        }

        // Step 5: Validate Amount
        if (!validateAmount(request.getAmount())) {
            throw new PaymentException("INVALID_AMOUNT", 
                "Amount must be between ₹1 and ₹1,00,000. Provided: " + request.getAmount());
        }

        // Step 6: Simulate bank processing delay
        simulateProcessing();

        // Step 7: Build successful response
        String transactionRef = generateTransactionRef();
        
        return TransferResponse.builder()
                .transactionRef(transactionRef)
                .status("SUCCESS")
                .statusCode("00")
                .payerVpa(request.getPayerVpa())
                .payeeVpa(request.getPayeeVpa())
                .amount(request.getAmount())
                .timestamp(LocalDateTime.now())
                .message("Transaction completed successfully")
                .build();
    }

    /**
     * Check transaction status by reference number
     */
    public String checkTransactionStatus(String transactionRef) {
        if (transactionRef == null || !transactionRef.startsWith("TXN")) {
            return "INVALID_REF";
        }
        // In real scenario, this would query database
        return "SUCCESS";
    }

    /**
     * Calculate transaction charges based on amount
     * UPI transactions are typically free, but some banks charge for merchant payments
     */
    public BigDecimal calculateCharges(BigDecimal amount, String transactionType) {
        if ("P2M".equals(transactionType) && amount.compareTo(new BigDecimal("2000")) > 0) {
            // 0.3% charge for merchant payments above ₹2000
            return amount.multiply(new BigDecimal("0.003")).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private void simulateProcessing() {
        try {
            Thread.sleep(PROCESSING_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
