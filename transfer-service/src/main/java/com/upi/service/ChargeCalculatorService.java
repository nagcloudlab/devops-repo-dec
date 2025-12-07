package com.upi.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Charge Calculator Service - Calculates transaction fees
 */
@Service
public class ChargeCalculatorService {

    // Fee percentages
    private static final BigDecimal P2P_FEE_PERCENTAGE = BigDecimal.ZERO;
    private static final BigDecimal P2M_FEE_PERCENTAGE = new BigDecimal("0.30");
    private static final BigDecimal BILL_FEE_PERCENTAGE = new BigDecimal("0.50");
    private static final BigDecimal GST_RATE = new BigDecimal("18.00");
    private static final BigDecimal MAX_FEE = new BigDecimal("750.00");

    /**
     * Calculate transaction charges
     */
    public ChargeResult calculateCharges(BigDecimal amount, String transactionType, boolean isInterBank) {
        ChargeResult result = new ChargeResult();
        result.setAmount(amount);
        result.setTransactionType(transactionType);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            result.setBaseFee(BigDecimal.ZERO);
            result.setGst(BigDecimal.ZERO);
            result.setTotalCharges(BigDecimal.ZERO);
            result.setNetAmount(amount != null ? amount : BigDecimal.ZERO);
            return result;
        }

        // Calculate base fee
        BigDecimal baseFee = calculateBaseFee(amount, transactionType);

        // Add inter-bank fee if applicable
        if (isInterBank && baseFee.compareTo(BigDecimal.ZERO) > 0) {
            baseFee = baseFee.add(new BigDecimal("2.00"));
        }

        // Cap at maximum
        if (baseFee.compareTo(MAX_FEE) > 0) {
            baseFee = MAX_FEE;
        }

        // Calculate GST
        BigDecimal gst = calculateGST(baseFee);

        // Total charges
        BigDecimal totalCharges = baseFee.add(gst);

        result.setBaseFee(baseFee);
        result.setGst(gst);
        result.setTotalCharges(totalCharges);
        result.setNetAmount(amount.add(totalCharges));

        return result;
    }

    /**
     * Calculate base fee based on transaction type
     */
    private BigDecimal calculateBaseFee(BigDecimal amount, String transactionType) {
        if (transactionType == null) {
            transactionType = "P2P";
        }

        BigDecimal percentage;
        switch (transactionType.toUpperCase()) {
            case "P2M":
            case "MERCHANT":
                percentage = P2M_FEE_PERCENTAGE;
                break;
            case "BILL":
            case "BILLPAY":
                percentage = BILL_FEE_PERCENTAGE;
                break;
            case "P2P":
            case "UPI":
            case "UPI_LITE":
            default:
                percentage = P2P_FEE_PERCENTAGE;
                break;
        }

        return amount.multiply(percentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate GST on fee
     */
    public BigDecimal calculateGST(BigDecimal baseFee) {
        if (baseFee == null || baseFee.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return baseFee.multiply(GST_RATE)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Check if transaction type is free
     */
    public boolean isTransactionFree(String transactionType) {
        if (transactionType == null) {
            return true;
        }
        switch (transactionType.toUpperCase()) {
            case "P2P":
            case "UPI":
            case "UPI_LITE":
                return true;
            default:
                return false;
        }
    }

    /**
     * Charge calculation result
     */
    public static class ChargeResult {
        private BigDecimal amount;
        private String transactionType;
        private BigDecimal baseFee;
        private BigDecimal gst;
        private BigDecimal totalCharges;
        private BigDecimal netAmount;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

        public BigDecimal getBaseFee() { return baseFee; }
        public void setBaseFee(BigDecimal baseFee) { this.baseFee = baseFee; }

        public BigDecimal getGst() { return gst; }
        public void setGst(BigDecimal gst) { this.gst = gst; }

        public BigDecimal getTotalCharges() { return totalCharges; }
        public void setTotalCharges(BigDecimal totalCharges) { this.totalCharges = totalCharges; }

        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

        public boolean isFreeTransaction() {
            return totalCharges != null && totalCharges.compareTo(BigDecimal.ZERO) == 0;
        }
    }
}
