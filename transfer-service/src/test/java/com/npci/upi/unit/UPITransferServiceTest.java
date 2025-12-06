package com.npci.upi.unit;

import com.npci.upi.service.UPITransferService;
import com.npci.upi.model.TransferRequest;
import com.npci.upi.model.TransferResponse;
import com.npci.upi.exception.PaymentException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * UNIT TESTS - UPI Transfer Service
 * ============================================================================
 * 
 * SHIFT-LEFT TESTING PRINCIPLE:
 * These tests run FIRST in the pipeline (within 2 minutes)
 * They catch 70% of bugs before code reaches integration stage
 * 
 * TEST STRUCTURE (Arrange-Act-Assert Pattern):
 * 1. Arrange: Set up test data
 * 2. Act: Call the method being tested
 * 3. Assert: Verify the expected outcome
 * 
 * ============================================================================
 */
@DisplayName("UPI Transfer Service - Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UPITransferServiceTest {

    private UPITransferService service;

    @BeforeEach
    void setUp() {
        service = new UPITransferService();
    }

    // ========================================================================
    // VPA VALIDATION TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("VPA Validation Tests")
    class VPAValidationTests {

        @Test
        @Order(1)
        @DisplayName("✅ Valid VPA formats should pass validation")
        void testValidVPAFormats() {
            // Common valid VPA patterns
            assertTrue(service.validateVPA("rahul@okicici"));
            assertTrue(service.validateVPA("merchant@ybl"));
            assertTrue(service.validateVPA("user123@paytm"));
            assertTrue(service.validateVPA("shop.owner@upi"));
            assertTrue(service.validateVPA("test-user@axisbank"));
        }

        @ParameterizedTest
        @Order(2)
        @DisplayName("❌ Invalid VPA formats should fail validation")
        @ValueSource(strings = {
            "invalid",           // No @ symbol
            "@okicici",          // No username
            "rahul@",            // No bank handle
            "ra@ok",             // Too short
            "user@123bank",      // Bank handle starts with number
            "user name@okicici", // Space in username
            "user@@okicici",     // Double @
        })
        void testInvalidVPAFormats(String invalidVpa) {
            assertFalse(service.validateVPA(invalidVpa), 
                "VPA should be invalid: " + invalidVpa);
        }

        @ParameterizedTest
        @Order(3)
        @DisplayName("❌ Null and empty VPA should fail validation")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        void testNullAndEmptyVPA(String vpa) {
            assertFalse(service.validateVPA(vpa));
        }
    }

    // ========================================================================
    // AMOUNT VALIDATION TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("Amount Validation Tests")
    class AmountValidationTests {

        @Test
        @Order(1)
        @DisplayName("✅ Valid amounts within UPI limits should pass")
        void testValidAmounts() {
            assertTrue(service.validateAmount(new BigDecimal("1.00")));      // Min limit
            assertTrue(service.validateAmount(new BigDecimal("100.50")));
            assertTrue(service.validateAmount(new BigDecimal("5000")));
            assertTrue(service.validateAmount(new BigDecimal("100000.00"))); // Max limit
        }

        @Test
        @Order(2)
        @DisplayName("❌ Amount below ₹1 should fail")
        void testAmountBelowMinimum() {
            assertFalse(service.validateAmount(new BigDecimal("0.99")));
            assertFalse(service.validateAmount(new BigDecimal("0")));
            assertFalse(service.validateAmount(new BigDecimal("-100")));
        }

        @Test
        @Order(3)
        @DisplayName("❌ Amount above ₹1,00,000 should fail")
        void testAmountAboveMaximum() {
            assertFalse(service.validateAmount(new BigDecimal("100000.01")));
            assertFalse(service.validateAmount(new BigDecimal("500000")));
        }

        @Test
        @Order(4)
        @DisplayName("❌ Null amount should fail")
        void testNullAmount() {
            assertFalse(service.validateAmount(null));
        }
    }

    // ========================================================================
    // TRANSACTION REFERENCE TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("Transaction Reference Tests")
    class TransactionRefTests {

        @Test
        @Order(1)
        @DisplayName("✅ Generated reference should follow TXN format")
        void testTransactionRefFormat() {
            String ref = service.generateTransactionRef();
            
            assertNotNull(ref);
            assertTrue(ref.startsWith("TXN"), "Reference should start with TXN");
            assertEquals(19, ref.length(), "Reference should be 19 characters");
        }

        @Test
        @Order(2)
        @DisplayName("✅ Each reference should be unique")
        void testTransactionRefUniqueness() {
            String ref1 = service.generateTransactionRef();
            String ref2 = service.generateTransactionRef();
            String ref3 = service.generateTransactionRef();
            
            assertNotEquals(ref1, ref2);
            assertNotEquals(ref2, ref3);
            assertNotEquals(ref1, ref3);
        }
    }

    // ========================================================================
    // TRANSFER PROCESSING TESTS - HAPPY PATH
    // ========================================================================
    
    @Nested
    @DisplayName("Transfer Processing - Happy Path")
    class TransferHappyPathTests {

        @Test
        @Order(1)
        @DisplayName("✅ Valid P2P transfer should succeed")
        void testSuccessfulP2PTransfer() throws PaymentException {
            // Arrange
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("sender@okicici")
                    .payeeVpa("receiver@ybl")
                    .amount(new BigDecimal("500.00"))
                    .transactionType("P2P")
                    .remarks("Test payment")
                    .build();

            // Act
            TransferResponse response = service.processTransfer(request);

            // Assert
            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("00", response.getStatusCode());
            assertNotNull(response.getTransactionRef());
            assertTrue(response.getTransactionRef().startsWith("TXN"));
            assertEquals(new BigDecimal("500.00"), response.getAmount());
        }

        @Test
        @Order(2)
        @DisplayName("✅ Minimum amount transfer should succeed")
        void testMinimumAmountTransfer() throws PaymentException {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@paytm")
                    .amount(new BigDecimal("1.00"))
                    .build();

            TransferResponse response = service.processTransfer(request);
            
            assertEquals("SUCCESS", response.getStatus());
        }

        @Test
        @Order(3)
        @DisplayName("✅ Maximum amount transfer should succeed")
        void testMaximumAmountTransfer() throws PaymentException {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("corporate@okicici")
                    .payeeVpa("vendor@ybl")
                    .amount(new BigDecimal("100000.00"))
                    .build();

            TransferResponse response = service.processTransfer(request);
            
            assertEquals("SUCCESS", response.getStatus());
        }
    }

    // ========================================================================
    // TRANSFER PROCESSING TESTS - ERROR SCENARIOS
    // ========================================================================
    
    @Nested
    @DisplayName("Transfer Processing - Error Scenarios")
    class TransferErrorTests {

        @Test
        @Order(1)
        @DisplayName("❌ Null request should throw PaymentException")
        void testNullRequest() {
            PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.processTransfer(null)
            );
            
            assertEquals("INVALID_REQUEST", exception.getErrorCode());
        }

        @Test
        @Order(2)
        @DisplayName("❌ Invalid payer VPA should throw PaymentException")
        void testInvalidPayerVPA() {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("invalid-vpa")
                    .payeeVpa("receiver@ybl")
                    .amount(new BigDecimal("100"))
                    .build();

            PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.processTransfer(request)
            );
            
            assertEquals("INVALID_PAYER_VPA", exception.getErrorCode());
        }

        @Test
        @Order(3)
        @DisplayName("❌ Invalid payee VPA should throw PaymentException")
        void testInvalidPayeeVPA() {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("sender@okicici")
                    .payeeVpa("bad-vpa")
                    .amount(new BigDecimal("100"))
                    .build();

            PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.processTransfer(request)
            );
            
            assertEquals("INVALID_PAYEE_VPA", exception.getErrorCode());
        }

        @Test
        @Order(4)
        @DisplayName("❌ Same payer and payee should throw PaymentException")
        void testSamePayerAndPayee() {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("user@okicici")
                    .amount(new BigDecimal("100"))
                    .build();

            PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.processTransfer(request)
            );
            
            assertEquals("SAME_ACCOUNT", exception.getErrorCode());
        }

        @Test
        @Order(5)
        @DisplayName("❌ Amount exceeding limit should throw PaymentException")
        void testAmountExceedingLimit() {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("sender@okicici")
                    .payeeVpa("receiver@ybl")
                    .amount(new BigDecimal("200000"))
                    .build();

            PaymentException exception = assertThrows(
                PaymentException.class,
                () -> service.processTransfer(request)
            );
            
            assertEquals("INVALID_AMOUNT", exception.getErrorCode());
        }
    }

    // ========================================================================
    // TRANSACTION CHARGES TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("Transaction Charges Calculation")
    class ChargesCalculationTests {

        @ParameterizedTest
        @Order(1)
        @DisplayName("✅ P2P transactions should have zero charges")
        @CsvSource({
            "100, P2P",
            "5000, P2P",
            "100000, P2P"
        })
        void testP2PZeroCharges(String amount, String type) {
            BigDecimal charges = service.calculateCharges(new BigDecimal(amount), type);
            assertEquals(BigDecimal.ZERO, charges);
        }

        @Test
        @Order(2)
        @DisplayName("✅ P2M above ₹2000 should have 0.3% charges")
        void testP2MChargesAboveThreshold() {
            BigDecimal charges = service.calculateCharges(new BigDecimal("3000"), "P2M");
            assertEquals(new BigDecimal("9.00"), charges); // 3000 * 0.3%
        }

        @Test
        @Order(3)
        @DisplayName("✅ P2M below ₹2000 should have zero charges")
        void testP2MChargesBelowThreshold() {
            BigDecimal charges = service.calculateCharges(new BigDecimal("1500"), "P2M");
            assertEquals(BigDecimal.ZERO, charges);
        }
    }

    // ========================================================================
    // TRANSACTION STATUS CHECK TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("Transaction Status Check")
    class StatusCheckTests {

        @Test
        @Order(1)
        @DisplayName("✅ Valid reference should return SUCCESS")
        void testValidReferenceStatus() {
            String status = service.checkTransactionStatus("TXN12345678ABCD123");
            assertEquals("SUCCESS", status);
        }

        @Test
        @Order(2)
        @DisplayName("❌ Invalid reference should return INVALID_REF")
        void testInvalidReferenceStatus() {
            assertEquals("INVALID_REF", service.checkTransactionStatus("INVALID123"));
            assertEquals("INVALID_REF", service.checkTransactionStatus(null));
            assertEquals("INVALID_REF", service.checkTransactionStatus(""));
        }
    }
}
