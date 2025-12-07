package com.upi.unit.contract;

import com.upi.dto.*;
import com.upi.entity.Transaction;
import com.upi.entity.Transaction.TransactionStatus;
import com.upi.service.ChargeCalculatorService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract Tests - DTO and Entity Schema Verification
 * 
 * TEST PYRAMID: CONTRACT TESTS
 * - Verifies data contracts (DTOs, Entities)
 * - Ensures backward compatibility
 * - No Spring context needed
 * - Tag: @Tag("contract")
 */
@DisplayName("📋 Contract Tests - Schema Verification")
@Tag("contract")
class ContractTest {

    // ========================================================================
    // TRANSFER REQUEST CONTRACT
    // ========================================================================

    @Nested
    @DisplayName("TransferRequest Contract")
    class TransferRequestContractTests {

        @Test
        @DisplayName("✅ TransferRequest should have all required fields")
        void transferRequest_shouldHaveAllFields() {
            TransferRequest request = new TransferRequest();
            request.setPayerVpa("payer@sbi");
            request.setPayeeVpa("payee@hdfc");
            request.setAmount(new BigDecimal("1000.00"));
            request.setTransactionType("P2P");
            request.setRemarks("Test payment");

            assertAll("TransferRequest fields",
                () -> assertEquals("payer@sbi", request.getPayerVpa()),
                () -> assertEquals("payee@hdfc", request.getPayeeVpa()),
                () -> assertEquals(0, new BigDecimal("1000.00").compareTo(request.getAmount())),
                () -> assertEquals("P2P", request.getTransactionType()),
                () -> assertEquals("Test payment", request.getRemarks())
            );
        }

        @Test
        @DisplayName("✅ TransferRequest default transaction type should be P2P")
        void transferRequest_defaultTypeShouldBeP2P() {
            TransferRequest request = new TransferRequest("payer@sbi", "payee@hdfc", new BigDecimal("100"));
            assertEquals("P2P", request.getTransactionType());
        }

        @Test
        @DisplayName("✅ TransferRequest toString should work")
        void transferRequest_toStringShouldWork() {
            TransferRequest request = new TransferRequest("payer@sbi", "payee@hdfc", new BigDecimal("100"));
            String str = request.toString();
            
            assertAll("toString content",
                () -> assertTrue(str.contains("payer@sbi")),
                () -> assertTrue(str.contains("payee@hdfc"))
            );
        }
    }

    // ========================================================================
    // TRANSFER RESPONSE CONTRACT
    // ========================================================================

    @Nested
    @DisplayName("TransferResponse Contract")
    class TransferResponseContractTests {

        @Test
        @DisplayName("✅ TransferResponse should have all required fields")
        void transferResponse_shouldHaveAllFields() {
            TransferResponse response = TransferResponse.builder()
                .transactionRef("TXN123")
                .status(TransactionStatus.SUCCESS)
                .message("Transfer successful")
                .amount(new BigDecimal("1000"))
                .charges(new BigDecimal("3.54"))
                .totalAmount(new BigDecimal("1003.54"))
                .payerVpa("payer@sbi")
                .payeeVpa("payee@hdfc")
                .bankRrn("123456789")
                .build();

            assertAll("TransferResponse fields",
                () -> assertNotNull(response.getTransactionRef()),
                () -> assertNotNull(response.getStatus()),
                () -> assertNotNull(response.getMessage()),
                () -> assertNotNull(response.getTimestamp()),
                () -> assertNotNull(response.getAmount()),
                () -> assertNotNull(response.getCharges()),
                () -> assertNotNull(response.getTotalAmount()),
                () -> assertNotNull(response.getPayerVpa()),
                () -> assertNotNull(response.getPayeeVpa()),
                () -> assertNotNull(response.getBankRrn())
            );
        }

        @Test
        @DisplayName("✅ TransferResponse timestamp should be auto-set")
        void transferResponse_timestampShouldBeAutoSet() {
            TransferResponse response = new TransferResponse();
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("✅ TransferResponse constructor should work")
        void transferResponse_constructorShouldWork() {
            TransferResponse response = new TransferResponse("TXN123", TransactionStatus.SUCCESS, "OK");
            
            assertAll("Constructor",
                () -> assertEquals("TXN123", response.getTransactionRef()),
                () -> assertEquals(TransactionStatus.SUCCESS, response.getStatus()),
                () -> assertEquals("OK", response.getMessage())
            );
        }
    }

    // ========================================================================
    // VALIDATION RESPONSE CONTRACT
    // ========================================================================

    @Nested
    @DisplayName("ValidationResponse Contract")
    class ValidationResponseContractTests {

        @Test
        @DisplayName("✅ ValidationResponse should have all required fields")
        void validationResponse_shouldHaveAllFields() {
            ValidationResponse response = new ValidationResponse();
            response.setVpa("user@sbi");
            response.setValid(true);
            response.setUsername("user");
            response.setBankHandle("sbi");
            response.addError("Test error");
            response.addWarning("Test warning");

            assertAll("ValidationResponse fields",
                () -> assertEquals("user@sbi", response.getVpa()),
                () -> assertTrue(response.isValid()),
                () -> assertEquals("user", response.getUsername()),
                () -> assertEquals("sbi", response.getBankHandle()),
                () -> assertTrue(response.hasErrors()),
                () -> assertTrue(response.hasWarnings())
            );
        }

        @Test
        @DisplayName("✅ ValidationResponse empty errors/warnings by default")
        void validationResponse_emptyErrorsWarningsByDefault() {
            ValidationResponse response = new ValidationResponse("user@sbi", true);
            
            assertFalse(response.hasErrors());
            assertFalse(response.hasWarnings());
        }
    }

    // ========================================================================
    // API ERROR CONTRACT
    // ========================================================================

    @Nested
    @DisplayName("ApiError Contract")
    class ApiErrorContractTests {

        @Test
        @DisplayName("✅ ApiError should have all required fields")
        void apiError_shouldHaveAllFields() {
            ApiError error = new ApiError(400, "VALIDATION_ERROR", "Invalid request", "/api/v1/transfer");

            assertAll("ApiError fields",
                () -> assertEquals(400, error.getStatus()),
                () -> assertEquals("VALIDATION_ERROR", error.getError()),
                () -> assertEquals("Invalid request", error.getMessage()),
                () -> assertEquals("/api/v1/transfer", error.getPath()),
                () -> assertNotNull(error.getTimestamp())
            );
        }

        @Test
        @DisplayName("✅ ApiError should support field errors")
        void apiError_shouldSupportFieldErrors() {
            ApiError error = new ApiError(400, "VALIDATION_ERROR", "Invalid request");
            error.addFieldError("amount", "Amount is required");
            error.addFieldError("payerVpa", "Invalid VPA format");

            assertEquals(2, error.getFieldErrors().size());
            assertEquals("amount", error.getFieldErrors().get(0).getField());
        }

        @Test
        @DisplayName("✅ ApiError timestamp should be auto-set")
        void apiError_timestampShouldBeAutoSet() {
            ApiError error = new ApiError();
            assertNotNull(error.getTimestamp());
        }
    }

    // ========================================================================
    // TRANSACTION ENTITY CONTRACT
    // ========================================================================

    @Nested
    @DisplayName("Transaction Entity Contract")
    class TransactionEntityContractTests {

        @Test
        @DisplayName("✅ Transaction entity should have all required fields")
        void transactionEntity_shouldHaveAllFields() {
            Transaction txn = Transaction.builder()
                .transactionRef("TXN123")
                .payerVpa("payer@sbi")
                .payeeVpa("payee@hdfc")
                .amount(new BigDecimal("1000"))
                .charges(new BigDecimal("0"))
                .transactionType("P2P")
                .remarks("Test")
                .build();

            assertAll("Transaction entity fields",
                () -> assertNotNull(txn.getTransactionRef()),
                () -> assertNotNull(txn.getPayerVpa()),
                () -> assertNotNull(txn.getPayeeVpa()),
                () -> assertNotNull(txn.getAmount()),
                () -> assertNotNull(txn.getStatus()),
                () -> assertNotNull(txn.getCreatedAt())
            );
        }

        @Test
        @DisplayName("✅ Transaction status enum should have all values")
        void transactionStatus_shouldHaveAllValues() {
            TransactionStatus[] statuses = TransactionStatus.values();

            assertTrue(statuses.length >= 7, "Should have at least 7 status values");

            assertAll("Status values",
                () -> assertNotNull(TransactionStatus.valueOf("INITIATED")),
                () -> assertNotNull(TransactionStatus.valueOf("PENDING")),
                () -> assertNotNull(TransactionStatus.valueOf("PROCESSING")),
                () -> assertNotNull(TransactionStatus.valueOf("SUCCESS")),
                () -> assertNotNull(TransactionStatus.valueOf("FAILED")),
                () -> assertNotNull(TransactionStatus.valueOf("REVERSED")),
                () -> assertNotNull(TransactionStatus.valueOf("TIMEOUT")),
                () -> assertNotNull(TransactionStatus.valueOf("CANCELLED"))
            );
        }

        @Test
        @DisplayName("✅ Transaction default status should be INITIATED")
        void transaction_defaultStatusShouldBeInitiated() {
            Transaction txn = new Transaction();
            assertEquals(TransactionStatus.INITIATED, txn.getStatus());
        }

        @Test
        @DisplayName("✅ Transaction createdAt should be auto-set")
        void transaction_createdAtShouldBeAutoSet() {
            Transaction txn = new Transaction();
            assertNotNull(txn.getCreatedAt());
        }

        @Test
        @DisplayName("✅ Transaction processing time calculation should work")
        void transaction_processingTimeShouldWork() {
            Transaction txn = new Transaction();
            txn.setCreatedAt(LocalDateTime.now().minusSeconds(5));
            txn.setCompletedAt(LocalDateTime.now());

            long processingTime = txn.getProcessingTimeMs();
            assertTrue(processingTime >= 4000 && processingTime <= 6000);
        }

        @Test
        @DisplayName("✅ Transaction toString should work")
        void transaction_toStringShouldWork() {
            Transaction txn = Transaction.builder()
                .transactionRef("TXN123")
                .payerVpa("payer@sbi")
                .payeeVpa("payee@hdfc")
                .amount(new BigDecimal("1000"))
                .build();

            String str = txn.toString();
            assertTrue(str.contains("TXN123"));
            assertTrue(str.contains("payer@sbi"));
        }
    }

    // ========================================================================
    // CHARGE RESULT CONTRACT
    // ========================================================================

    @Nested
    @DisplayName("ChargeResult Contract")
    class ChargeResultContractTests {

        @Test
        @DisplayName("✅ ChargeResult should have all required fields")
        void chargeResult_shouldHaveAllFields() {
            ChargeCalculatorService.ChargeResult result = new ChargeCalculatorService.ChargeResult();
            result.setAmount(new BigDecimal("1000"));
            result.setTransactionType("P2M");
            result.setBaseFee(new BigDecimal("3"));
            result.setGst(new BigDecimal("0.54"));
            result.setTotalCharges(new BigDecimal("3.54"));
            result.setNetAmount(new BigDecimal("1003.54"));

            assertAll("ChargeResult fields",
                () -> assertNotNull(result.getAmount()),
                () -> assertNotNull(result.getTransactionType()),
                () -> assertNotNull(result.getBaseFee()),
                () -> assertNotNull(result.getGst()),
                () -> assertNotNull(result.getTotalCharges()),
                () -> assertNotNull(result.getNetAmount())
            );
        }

        @Test
        @DisplayName("✅ ChargeResult isFreeTransaction should work")
        void chargeResult_isFreeTransactionShouldWork() {
            ChargeCalculatorService.ChargeResult freeResult = new ChargeCalculatorService.ChargeResult();
            freeResult.setTotalCharges(BigDecimal.ZERO);
            
            ChargeCalculatorService.ChargeResult paidResult = new ChargeCalculatorService.ChargeResult();
            paidResult.setTotalCharges(new BigDecimal("3.54"));

            assertTrue(freeResult.isFreeTransaction());
            assertFalse(paidResult.isFreeTransaction());
        }
    }

    // ========================================================================
    // VALIDATION REQUEST CONTRACT
    // ========================================================================

    @Nested
    @DisplayName("ValidationRequest Contract")
    class ValidationRequestContractTests {

        @Test
        @DisplayName("✅ ValidationRequest should have VPA field")
        void validationRequest_shouldHaveVpaField() {
            ValidationRequest request = new ValidationRequest("user@sbi");
            assertEquals("user@sbi", request.getVpa());
        }

        @Test
        @DisplayName("✅ ValidationRequest default constructor should work")
        void validationRequest_defaultConstructorShouldWork() {
            ValidationRequest request = new ValidationRequest();
            request.setVpa("test@hdfc");
            assertEquals("test@hdfc", request.getVpa());
        }
    }
}
