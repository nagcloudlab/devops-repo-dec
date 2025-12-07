package com.upi.service;

import com.upi.dto.ValidationResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * VPA Validator Service - Validates Virtual Payment Addresses
 */
@Service
public class VpaValidatorService {

    // VPA format pattern: username@handle
    private static final Pattern VPA_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9][a-zA-Z0-9._-]{0,49}@[a-zA-Z][a-zA-Z0-9]{1,20}$"
    );

    // Valid bank handles
    private static final Set<String> VALID_BANK_HANDLES = new HashSet<>(Arrays.asList(
            "sbi", "hdfc", "icici", "axis", "pnb", "boi", "bob", "canara", "union",
            "kotak", "indus", "yes", "idbi", "federal", "rbl", "dcb", "kvb", "csb",
            "paytm", "ybl", "okhdfcbank", "okicici", "okaxis", "oksbi", "apl",
            "upi", "gpay", "phonepe", "amazonpay", "freecharge", "mobikwik",
            "idfcbank", "axisbank", "hdfcbank", "icicibank", "sbibank"
    ));

    // Blocked patterns
    private static final List<String> BLOCKED_PATTERNS = Arrays.asList(
            "test", "admin", "root", "system", "null", "undefined", "blocked", "fraud"
    );

    /**
     * Check if VPA format is valid
     */
    public boolean isValidFormat(String vpa) {
        if (vpa == null || vpa.trim().isEmpty()) {
            return false;
        }
        return VPA_PATTERN.matcher(vpa.trim()).matches();
    }

    /**
     * Check if bank handle is known/valid
     */
    public boolean hasValidBankHandle(String vpa) {
        if (!isValidFormat(vpa)) {
            return false;
        }
        String handle = extractBankHandle(vpa);
        return handle != null && VALID_BANK_HANDLES.contains(handle.toLowerCase());
    }

    /**
     * Extract bank handle from VPA
     */
    public String extractBankHandle(String vpa) {
        if (vpa == null || !vpa.contains("@")) {
            return null;
        }
        String[] parts = vpa.split("@");
        return parts.length == 2 ? parts[1].toLowerCase() : null;
    }

    /**
     * Extract username from VPA
     */
    public String extractUsername(String vpa) {
        if (vpa == null || !vpa.contains("@")) {
            return null;
        }
        String[] parts = vpa.split("@");
        return parts.length >= 1 ? parts[0] : null;
    }

    /**
     * Check if VPA contains blocked patterns
     */
    public boolean containsBlockedPattern(String vpa) {
        if (vpa == null) {
            return false;
        }
        String lowerVpa = vpa.toLowerCase();
        return BLOCKED_PATTERNS.stream().anyMatch(lowerVpa::contains);
    }

    /**
     * Full validation with detailed response
     */
    public ValidationResponse validate(String vpa) {
        ValidationResponse response = new ValidationResponse();
        response.setVpa(vpa);

        // Check null/empty
        if (vpa == null || vpa.trim().isEmpty()) {
            response.setValid(false);
            response.addError("VPA cannot be null or empty");
            return response;
        }

        String trimmedVpa = vpa.trim();

        // Check format
        if (!isValidFormat(trimmedVpa)) {
            response.setValid(false);
            response.addError("Invalid VPA format. Expected: username@bankhandle");
            return response;
        }

        // Check blocked patterns
        if (containsBlockedPattern(trimmedVpa)) {
            response.setValid(false);
            response.addError("VPA contains blocked/reserved pattern");
            return response;
        }

        // Extract and validate components
        String handle = extractBankHandle(trimmedVpa);
        String username = extractUsername(trimmedVpa);

        response.setUsername(username);
        response.setBankHandle(handle);

        // Check bank handle
        if (!VALID_BANK_HANDLES.contains(handle.toLowerCase())) {
            response.setValid(false);
            response.addError("Unknown bank handle: " + handle);
            response.addWarning("Handle may be valid but not in approved list");
        } else {
            response.setValid(true);
        }

        return response;
    }

    /**
     * Check if two VPAs are different
     */
    public boolean areDifferentVPAs(String payerVpa, String payeeVpa) {
        if (payerVpa == null || payeeVpa == null) {
            return false;
        }
        return !payerVpa.equalsIgnoreCase(payeeVpa);
    }

    /**
     * Check if same bank
     */
    public boolean isSameBank(String vpa1, String vpa2) {
        String handle1 = extractBankHandle(vpa1);
        String handle2 = extractBankHandle(vpa2);
        if (handle1 == null || handle2 == null) {
            return false;
        }
        return handle1.equalsIgnoreCase(handle2);
    }

    /**
     * Normalize VPA to lowercase
     */
    public String normalize(String vpa) {
        if (vpa == null) {
            return null;
        }
        return vpa.trim().toLowerCase();
    }

    /**
     * Get all valid bank handles
     */
    public Set<String> getValidBankHandles() {
        return Collections.unmodifiableSet(VALID_BANK_HANDLES);
    }
}
