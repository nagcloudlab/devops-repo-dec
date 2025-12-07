package com.upi.unit.service;

import com.upi.dto.ValidationResponse;
import com.upi.service.VpaValidatorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for VpaValidatorService
 * 
 * TEST PYRAMID: UNIT TESTS (70%)
 * - No dependencies, pure logic testing
 */
@DisplayName("VpaValidatorService - Unit Tests")
class VpaValidatorServiceTest {

    private VpaValidatorService validator;

    @BeforeEach
    void setUp() {
        validator = new VpaValidatorService();
    }

    // ========================================================================
    // FORMAT VALIDATION
    // ========================================================================

    @Nested
    @DisplayName("VPA Format Validation")
    class FormatValidationTests {

        @ParameterizedTest(name = "Valid format: {0}")
        @DisplayName("✅ Valid VPA formats should pass")
        @ValueSource(strings = {
            "user@sbi",
            "john.doe@hdfc",
            "test123@icici",
            "user_name@axis",
            "ab@xy",
            "mobile9876543210@paytm"
        })
        void validFormats_shouldPass(String vpa) {
            assertTrue(validator.isValidFormat(vpa), 
                "VPA should be valid: " + vpa);
        }

        @ParameterizedTest(name = "Invalid format: {0}")
        @DisplayName("❌ Invalid VPA formats should fail")
        @NullAndEmptySource
        @ValueSource(strings = {
            "   ",
            "noatsymbol",
            "@nouser",
            "nobank@",
            "space in@handle",
            "user@@double",
            ".startswithdot@bank"
        })
        void invalidFormats_shouldFail(String vpa) {
            assertFalse(validator.isValidFormat(vpa),
                "VPA should be invalid: " + vpa);
        }
    }

    // ========================================================================
    // BANK HANDLE VALIDATION
    // ========================================================================

    @Nested
    @DisplayName("Bank Handle Validation")
    class BankHandleTests {

        @ParameterizedTest(name = "Known handle: {0}")
        @DisplayName("✅ Known bank handles should be valid")
        @ValueSource(strings = {"sbi", "hdfc", "icici", "axis", "paytm", "ybl", "gpay"})
        void knownHandles_shouldBeValid(String handle) {
            String vpa = "user@" + handle;
            assertTrue(validator.hasValidBankHandle(vpa));
        }

        @ParameterizedTest(name = "Unknown handle: {0}")
        @DisplayName("❌ Unknown bank handles should be invalid")
        @ValueSource(strings = {"unknownbank", "fakebank", "xyz123"})
        void unknownHandles_shouldBeInvalid(String handle) {
            String vpa = "user@" + handle;
            assertFalse(validator.hasValidBankHandle(vpa));
        }

        @Test
        @DisplayName("Extract bank handle should work correctly")
        void extractBankHandle_shouldWork() {
            assertEquals("sbi", validator.extractBankHandle("user@sbi"));
            assertEquals("hdfc", validator.extractBankHandle("john.doe@HDFC"));
            assertNull(validator.extractBankHandle("invalidvpa"));
            assertNull(validator.extractBankHandle(null));
        }

        @Test
        @DisplayName("Extract username should work correctly")
        void extractUsername_shouldWork() {
            assertEquals("user", validator.extractUsername("user@sbi"));
            assertEquals("john.doe", validator.extractUsername("john.doe@hdfc"));
            assertNull(validator.extractUsername("invalidvpa"));
            assertNull(validator.extractUsername(null));
        }
    }

    // ========================================================================
    // BLOCKED PATTERN DETECTION
    // ========================================================================

    @Nested
    @DisplayName("Blocked Pattern Detection")
    class BlockedPatternTests {

        @ParameterizedTest(name = "Blocked: {0}")
        @DisplayName("❌ VPAs with blocked patterns should be flagged")
        @ValueSource(strings = {"testuser@sbi", "admin@hdfc", "root@icici", "fraud@axis"})
        void blockedPatterns_shouldBeFlagged(String vpa) {
            assertTrue(validator.containsBlockedPattern(vpa));
        }

        @Test
        @DisplayName("✅ Normal VPAs should not be flagged")
        void normalVPAs_shouldNotBeFlagged() {
            assertFalse(validator.containsBlockedPattern("user@sbi"));
            assertFalse(validator.containsBlockedPattern("merchant@hdfc"));
        }
    }

    // ========================================================================
    // COMPREHENSIVE VALIDATION
    // ========================================================================

    @Nested
    @DisplayName("Comprehensive Validation")
    class ComprehensiveValidationTests {

        @Test
        @DisplayName("✅ Valid VPA should return success result")
        void validVPA_shouldReturnSuccessResult() {
            ValidationResponse result = validator.validate("user@sbi");

            assertAll("Validation result",
                () -> assertTrue(result.isValid()),
                () -> assertEquals("user", result.getUsername()),
                () -> assertEquals("sbi", result.getBankHandle()),
                () -> assertFalse(result.hasErrors())
            );
        }

        @Test
        @DisplayName("❌ Invalid format should return error")
        void invalidFormat_shouldReturnError() {
            ValidationResponse result = validator.validate("invalid");

            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
        }

        @Test
        @DisplayName("❌ Null VPA should return error")
        void nullVPA_shouldReturnError() {
            ValidationResponse result = validator.validate(null);

            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
        }
    }

    // ========================================================================
    // VPA COMPARISON
    // ========================================================================

    @Nested
    @DisplayName("VPA Comparison")
    class VPAComparisonTests {

        @Test
        @DisplayName("✅ Different VPAs should be detected")
        void differentVPAs_shouldBeDetected() {
            assertTrue(validator.areDifferentVPAs("user1@sbi", "user2@sbi"));
            assertTrue(validator.areDifferentVPAs("user@sbi", "user@hdfc"));
        }

        @Test
        @DisplayName("❌ Same VPAs should be detected")
        void sameVPAs_shouldBeDetected() {
            assertFalse(validator.areDifferentVPAs("user@sbi", "user@sbi"));
            assertFalse(validator.areDifferentVPAs("user@sbi", "USER@SBI"));
        }

        @Test
        @DisplayName("Same bank should be detected")
        void sameBank_shouldBeDetected() {
            assertTrue(validator.isSameBank("user1@sbi", "user2@sbi"));
            assertFalse(validator.isSameBank("user@sbi", "user@hdfc"));
        }
    }

    // ========================================================================
    // NORMALIZATION
    // ========================================================================

    @Nested
    @DisplayName("VPA Normalization")
    class NormalizationTests {

        @Test
        @DisplayName("VPA should be normalized to lowercase")
        void vpa_shouldBeLowercase() {
            assertEquals("user@sbi", validator.normalize("USER@SBI"));
            assertEquals("user@sbi", validator.normalize("User@Sbi"));
        }

        @Test
        @DisplayName("VPA should be trimmed")
        void vpa_shouldBeTrimmed() {
            assertEquals("user@sbi", validator.normalize("  user@sbi  "));
        }

        @Test
        @DisplayName("Null should return null")
        void null_shouldReturnNull() {
            assertNull(validator.normalize(null));
        }
    }
}
