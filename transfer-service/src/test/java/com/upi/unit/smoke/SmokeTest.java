package com.upi.unit.smoke;

import com.upi.dto.ValidationResponse;
import com.upi.service.ChargeCalculatorService;
import com.upi.service.VpaValidatorService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke Tests - Quick sanity checks for critical functionality
 * 
 * TEST PYRAMID: SMOKE TESTS
 * - Run before every deployment
 * - Fast execution (< 5 seconds total)
 * - Tests critical paths only
 * - No Spring context needed
 * - Tag: @Tag("smoke")
 */
@DisplayName("🚨 Smoke Tests - Critical Path Verification")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("smoke")
class SmokeTest {

    private VpaValidatorService vpaValidatorService;
    private ChargeCalculatorService chargeCalculatorService;

    @BeforeEach
    void setUp() {
        vpaValidatorService = new VpaValidatorService();
        chargeCalculatorService = new ChargeCalculatorService();
    }

    // ========================================================================
    // SERVICE INSTANTIATION
    // ========================================================================

    @Test
    @Order(1)
    @DisplayName("🔥 1. Services can be instantiated")
    void servicesCanBeInstantiated() {
        assertNotNull(vpaValidatorService, "VpaValidatorService should be instantiated");
        assertNotNull(chargeCalculatorService, "ChargeCalculatorService should be instantiated");
    }

    // ========================================================================
    // VPA VALIDATION - SMOKE
    // ========================================================================

    @Test
    @Order(2)
    @DisplayName("🔥 2. VPA validation works")
    void vpaValidationWorks() {
        // Valid VPA
        assertTrue(vpaValidatorService.isValidFormat("user@sbi"));
        
        // Invalid VPA
        assertFalse(vpaValidatorService.isValidFormat("invalid"));
    }

    @Test
    @Order(3)
    @DisplayName("🔥 3. VPA comprehensive validation returns response")
    void vpaComprehensiveValidationWorks() {
        ValidationResponse response = vpaValidatorService.validate("merchant@hdfc");
        
        assertAll("VPA validation response",
            () -> assertNotNull(response),
            () -> assertTrue(response.isValid()),
            () -> assertEquals("merchant", response.getUsername()),
            () -> assertEquals("hdfc", response.getBankHandle())
        );
    }

    // ========================================================================
    // CHARGE CALCULATION - SMOKE
    // ========================================================================

    @Test
    @Order(4)
    @DisplayName("🔥 4. Charge calculation works")
    void chargeCalculationWorks() {
        var result = chargeCalculatorService.calculateCharges(
            new BigDecimal("1000"), "P2P", false);
        
        assertAll("Charge calculation",
            () -> assertNotNull(result),
            () -> assertNotNull(result.getTotalCharges()),
            () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()), 
                "P2P should be free")
        );
    }

    @Test
    @Order(5)
    @DisplayName("🔥 5. P2M charges are calculated correctly")
    void p2mChargesWork() {
        var result = chargeCalculatorService.calculateCharges(
            new BigDecimal("10000"), "P2M", false);
        
        assertNotNull(result.getBaseFee());
        assertTrue(result.getBaseFee().compareTo(BigDecimal.ZERO) > 0, 
            "P2M should have charges");
    }

    // ========================================================================
    // BANK HANDLE VALIDATION - SMOKE
    // ========================================================================

    @Test
    @Order(6)
    @DisplayName("🔥 6. Known bank handles are recognized")
    void knownBankHandlesRecognized() {
        assertTrue(vpaValidatorService.hasValidBankHandle("user@sbi"));
        assertTrue(vpaValidatorService.hasValidBankHandle("user@hdfc"));
        assertTrue(vpaValidatorService.hasValidBankHandle("user@icici"));
        assertTrue(vpaValidatorService.hasValidBankHandle("user@paytm"));
    }

    @Test
    @Order(7)
    @DisplayName("🔥 7. VPA utility methods work")
    void vpaUtilityMethodsWork() {
        assertEquals("sbi", vpaValidatorService.extractBankHandle("user@sbi"));
        assertEquals("user", vpaValidatorService.extractUsername("user@sbi"));
        assertTrue(vpaValidatorService.areDifferentVPAs("user1@sbi", "user2@sbi"));
        assertTrue(vpaValidatorService.isSameBank("user1@sbi", "user2@sbi"));
    }

    // ========================================================================
    // BLOCKED PATTERNS - SMOKE
    // ========================================================================

    @Test
    @Order(8)
    @DisplayName("🔥 8. Blocked patterns are detected")
    void blockedPatternsDetected() {
        assertTrue(vpaValidatorService.containsBlockedPattern("frauduser@sbi"));
        assertTrue(vpaValidatorService.containsBlockedPattern("testuser@hdfc"));
        assertFalse(vpaValidatorService.containsBlockedPattern("normaluser@sbi"));
    }

    // ========================================================================
    // VALIDATION EDGE CASES - SMOKE
    // ========================================================================

    @Test
    @Order(9)
    @DisplayName("🔥 9. Null/empty VPA handling works")
    void nullEmptyVpaHandling() {
        assertFalse(vpaValidatorService.isValidFormat(null));
        assertFalse(vpaValidatorService.isValidFormat(""));
        assertFalse(vpaValidatorService.isValidFormat("   "));
    }

    @Test
    @Order(10)
    @DisplayName("🔥 10. VPA normalization works")
    void vpaNormalizationWorks() {
        assertEquals("user@sbi", vpaValidatorService.normalize("USER@SBI"));
        assertEquals("user@sbi", vpaValidatorService.normalize("  user@sbi  "));
        assertNull(vpaValidatorService.normalize(null));
    }

    // ========================================================================
    // CHARGE EDGE CASES - SMOKE
    // ========================================================================

    @Test
    @Order(11)
    @DisplayName("🔥 11. Charge calculation handles null/zero amounts")
    void chargeCalculationEdgeCases() {
        var nullResult = chargeCalculatorService.calculateCharges(null, "P2M", false);
        var zeroResult = chargeCalculatorService.calculateCharges(BigDecimal.ZERO, "P2M", false);
        
        assertEquals(0, BigDecimal.ZERO.compareTo(nullResult.getTotalCharges()));
        assertEquals(0, BigDecimal.ZERO.compareTo(zeroResult.getTotalCharges()));
    }

    @Test
    @Order(12)
    @DisplayName("🔥 12. Transaction type free check works")
    void transactionTypeFreeCheck() {
        assertTrue(chargeCalculatorService.isTransactionFree("P2P"));
        assertTrue(chargeCalculatorService.isTransactionFree("UPI"));
        assertFalse(chargeCalculatorService.isTransactionFree("P2M"));
        assertFalse(chargeCalculatorService.isTransactionFree("BILL"));
    }
}
