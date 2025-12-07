package com.upi.service;

import com.upi.dto.TransferRequest;
import com.upi.dto.TransferResponse;
import com.upi.entity.Transaction;
import com.upi.entity.Transaction.TransactionStatus;
import com.upi.exception.PaymentException;
import com.upi.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transfer Service - Main service for processing UPI transfers
 */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000.00");
    private static final AtomicLong counter = new AtomicLong(0);
    private static final Random random = new Random();

    private final TransactionRepository transactionRepository;
    private final VpaValidatorService vpaValidator;
    private final ChargeCalculatorService chargeCalculator;

    public TransferService(TransactionRepository transactionRepository,
                           VpaValidatorService vpaValidator,
                           ChargeCalculatorService chargeCalculator) {
        this.transactionRepository = transactionRepository;
        this.vpaValidator = vpaValidator;
        this.chargeCalculator = chargeCalculator;
    }

    /**
     * Process a fund transfer
     */
    @Transactional
    public TransferResponse processTransfer(TransferRequest request) {
        // Check for null request first
        if (request == null) {
            throw new PaymentException("Transfer request cannot be null", "INVALID_REQUEST");
        }
        
        log.info("Processing transfer request: {} -> {}, amount: {}",
                request.getPayerVpa(), request.getPayeeVpa(), request.getAmount());

        // Validate request
        validateTransferRequest(request);

        // Generate transaction reference
        String transactionRef = generateTransactionRef();

        // Calculate charges
        boolean isInterBank = !vpaValidator.isSameBank(request.getPayerVpa(), request.getPayeeVpa());
        var chargeResult = chargeCalculator.calculateCharges(
                request.getAmount(), request.getTransactionType(), isInterBank);

        // Create transaction entity
        Transaction transaction = Transaction.builder()
                .transactionRef(transactionRef)
                .payerVpa(request.getPayerVpa().toLowerCase())
                .payeeVpa(request.getPayeeVpa().toLowerCase())
                .amount(request.getAmount())
                .charges(chargeResult.getTotalCharges())
                .transactionType(request.getTransactionType())
                .remarks(request.getRemarks())
                .status(TransactionStatus.PROCESSING)
                .build();

        // Save transaction
        transaction = transactionRepository.save(transaction);

        // Simulate processing (in real system, this would call bank APIs)
        TransactionStatus finalStatus = simulateTransferProcessing(transaction);
        
        // Update status
        transaction.setStatus(finalStatus);
        transaction.setCompletedAt(LocalDateTime.now());
        if (finalStatus == TransactionStatus.SUCCESS) {
            transaction.setBankRrn(generateBankRrn());
        }
        transactionRepository.save(transaction);

        log.info("Transfer completed: ref={}, status={}", transactionRef, finalStatus);

        // Build response
        return TransferResponse.builder()
                .transactionRef(transactionRef)
                .status(finalStatus)
                .message(finalStatus == TransactionStatus.SUCCESS ? 
                        "Transfer completed successfully" : "Transfer failed")
                .amount(request.getAmount())
                .charges(chargeResult.getTotalCharges())
                .totalAmount(chargeResult.getNetAmount())
                .payerVpa(request.getPayerVpa())
                .payeeVpa(request.getPayeeVpa())
                .bankRrn(transaction.getBankRrn())
                .build();
    }

    /**
     * Get transaction status by reference
     */
    public TransferResponse getTransactionStatus(String transactionRef) {
        log.debug("Getting status for transaction: {}", transactionRef);

        Optional<Transaction> txnOpt = transactionRepository.findByTransactionRef(transactionRef);
        
        if (txnOpt.isEmpty()) {
            throw new PaymentException("Transaction not found: " + transactionRef, "TXN_NOT_FOUND");
        }

        Transaction txn = txnOpt.get();

        return TransferResponse.builder()
                .transactionRef(txn.getTransactionRef())
                .status(txn.getStatus())
                .message(getStatusMessage(txn.getStatus()))
                .amount(txn.getAmount())
                .charges(txn.getCharges())
                .payerVpa(txn.getPayerVpa())
                .payeeVpa(txn.getPayeeVpa())
                .bankRrn(txn.getBankRrn())
                .build();
    }

    /**
     * Get transaction history for a VPA
     */
    public List<Transaction> getTransactionHistory(String vpa) {
        return transactionRepository.findTop10ByPayerVpaOrderByCreatedAtDesc(vpa.toLowerCase());
    }

    /**
     * Check if VPA is valid
     */
    public boolean isValidVpa(String vpa) {
        return vpaValidator.isValidFormat(vpa) && !vpaValidator.containsBlockedPattern(vpa);
    }

    /**
     * Check if amount is valid
     */
    public boolean isValidAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
    }

    /**
     * Validate transfer request
     */
    private void validateTransferRequest(TransferRequest request) {
        if (request == null) {
            throw new PaymentException("Transfer request cannot be null", "INVALID_REQUEST");
        }

        // Validate payer VPA
        if (!isValidVpa(request.getPayerVpa())) {
            throw new PaymentException("Invalid payer VPA: " + request.getPayerVpa(), "INVALID_PAYER_VPA");
        }

        // Validate payee VPA
        if (!isValidVpa(request.getPayeeVpa())) {
            throw new PaymentException("Invalid payee VPA: " + request.getPayeeVpa(), "INVALID_PAYEE_VPA");
        }

        // Check same VPA
        if (!vpaValidator.areDifferentVPAs(request.getPayerVpa(), request.getPayeeVpa())) {
            throw new PaymentException("Payer and payee cannot be the same", "SAME_VPA");
        }

        // Validate amount
        if (!isValidAmount(request.getAmount())) {
            throw new PaymentException(
                    String.format("Amount must be between ₹%.2f and ₹%.2f", MIN_AMOUNT, MAX_AMOUNT),
                    "INVALID_AMOUNT"
            );
        }
    }

    /**
     * Generate unique transaction reference
     */
    public String generateTransactionRef() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String suffix = String.format("%02d", counter.incrementAndGet() % 100);
        return "TXN" + timestamp + suffix;
    }

    /**
     * Generate bank reference number
     */
    private String generateBankRrn() {
        return String.format("%012d", random.nextLong() % 1000000000000L);
    }

    /**
     * Simulate transfer processing
     */
    private TransactionStatus simulateTransferProcessing(Transaction transaction) {
        // In production, this would:
        // 1. Call payer's bank to debit
        // 2. Call payee's bank to credit
        // 3. Handle failures and reversals
        
        // For demo, always succeed unless VPA contains "fail"
        if (transaction.getPayerVpa().contains("fail") || 
            transaction.getPayeeVpa().contains("fail")) {
            return TransactionStatus.FAILED;
        }
        return TransactionStatus.SUCCESS;
    }

    /**
     * Get status message
     */
    private String getStatusMessage(TransactionStatus status) {
        switch (status) {
            case SUCCESS: return "Transfer completed successfully";
            case FAILED: return "Transfer failed";
            case PENDING: return "Transfer is pending";
            case PROCESSING: return "Transfer is being processed";
            case REVERSED: return "Transfer has been reversed";
            case TIMEOUT: return "Transfer timed out";
            case CANCELLED: return "Transfer was cancelled";
            default: return "Unknown status";
        }
    }
}
