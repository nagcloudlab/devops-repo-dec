package com.upi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upi.dto.TransferRequest;
import com.upi.dto.ValidationRequest;
import com.upi.entity.Transaction.TransactionStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Controller Tests - REST endpoint testing
 * 
 * TEST PYRAMID: API TESTS (8%)
 * - Tests HTTP layer
 * - Request/Response validation
 * - Error handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("API Controller Tests")
class TransferControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================================================
    // TRANSFER ENDPOINT - POST /api/v1/transfer
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/transfer - Fund Transfer")
    class TransferEndpointTests {

        @Test
        @DisplayName("✅ 201 - Valid transfer request should succeed")
        void validTransferRequest_shouldReturn201() throws Exception {
            TransferRequest request = createValidRequest();

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionRef").exists())
                .andExpect(jsonPath("$.transactionRef", startsWith("TXN")))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.payerVpa").value("payer@sbi"))
                .andExpect(jsonPath("$.payeeVpa").value("payee@hdfc"));
        }

        @Test
        @DisplayName("❌ 400 - Missing payer VPA should return validation error")
        void missingPayerVpa_shouldReturn400() throws Exception {
            TransferRequest request = createValidRequest();
            request.setPayerVpa(null);

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("payerVpa"));
        }

        @Test
        @DisplayName("❌ 400 - Invalid payer VPA format should return validation error")
        void invalidPayerVpaFormat_shouldReturn400() throws Exception {
            TransferRequest request = createValidRequest();
            request.setPayerVpa("invalid-vpa");

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("❌ 400 - Amount below minimum should return error")
        void amountBelowMinimum_shouldReturn400() throws Exception {
            TransferRequest request = createValidRequest();
            request.setAmount(new BigDecimal("0.50"));

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("amount"));
        }

        @Test
        @DisplayName("❌ 400 - Amount above maximum should return error")
        void amountAboveMaximum_shouldReturn400() throws Exception {
            TransferRequest request = createValidRequest();
            request.setAmount(new BigDecimal("200000"));

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("amount"));
        }

        @Test
        @DisplayName("❌ 400 - Same payer and payee should return error")
        void samePayerAndPayee_shouldReturn400() throws Exception {
            TransferRequest request = createValidRequest();
            request.setPayeeVpa("payer@sbi"); // Same as payer

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SAME_VPA"));
        }

        @Test
        @DisplayName("❌ 400 - Negative amount should return error")
        void negativeAmount_shouldReturn400() throws Exception {
            TransferRequest request = createValidRequest();
            request.setAmount(new BigDecimal("-100"));

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("✅ Transfer with remarks should succeed")
        void transferWithRemarks_shouldSucceed() throws Exception {
            TransferRequest request = createValidRequest();
            request.setRemarks("Payment for groceries");

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        }

        @Test
        @DisplayName("✅ P2M transfer should include charges")
        void p2mTransfer_shouldIncludeCharges() throws Exception {
            TransferRequest request = createValidRequest();
            request.setTransactionType("P2M");
            request.setAmount(new BigDecimal("10000"));

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.charges").exists());
        }
    }

    // ========================================================================
    // STATUS ENDPOINT - GET /api/v1/transfer/{transactionRef}
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/transfer/{transactionRef} - Transaction Status")
    class StatusEndpointTests {

        @Test
        @DisplayName("✅ 200 - Should return status for existing transaction")
        void existingTransaction_shouldReturn200() throws Exception {
            // First create a transaction
            TransferRequest request = createValidRequest();
            MvcResult createResult = mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            // Extract transaction reference
            String response = createResult.getResponse().getContentAsString();
            String transactionRef = objectMapper.readTree(response).get("transactionRef").asText();

            // Get status
            mockMvc.perform(get("/api/v1/transfer/{transactionRef}", transactionRef))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionRef").value(transactionRef))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        }

        @Test
        @DisplayName("❌ 404 - Non-existent transaction should return error")
        void nonExistentTransaction_shouldReturn404() throws Exception {
            mockMvc.perform(get("/api/v1/transfer/{transactionRef}", "NON_EXISTENT_REF"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("TXN_NOT_FOUND"));
        }
    }

    // ========================================================================
    // HISTORY ENDPOINT - GET /api/v1/transfer/history/{vpa}
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/transfer/history/{vpa} - Transaction History")
    class HistoryEndpointTests {

        @Test
        @DisplayName("✅ 200 - Should return history for VPA")
        void existingVpa_shouldReturnHistory() throws Exception {
            // Create some transactions
            TransferRequest request = createValidRequest();
            request.setPayerVpa("history.user@sbi");

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            // Get history - USE THE SAME VPA AS THE POST REQUEST
            mockMvc.perform(get("/api/v1/transfer/history/{vpa}", "history.user@sbi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].payerVpa").value("history.user@sbi"));
        }

        @Test
        @DisplayName("✅ 200 - Should return empty array for new VPA")
        void newVpa_shouldReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/transfer/history/{vpa}", "brand.new@sbi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ========================================================================
    // VALIDATION ENDPOINT - POST /api/v1/validate/vpa
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/validate/vpa - VPA Validation")
    class VpaValidationEndpointTests {

        @Test
        @DisplayName("✅ 200 - Valid VPA should return success")
        void validVpa_shouldReturnSuccess() throws Exception {
            ValidationRequest request = new ValidationRequest("user@sbi");

            mockMvc.perform(post("/api/v1/validate/vpa")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.bankHandle").value("sbi"));
        }

        @Test
        @DisplayName("✅ 200 - Invalid VPA should return validation errors")
        void invalidVpa_shouldReturnErrors() throws Exception {
            ValidationRequest request = new ValidationRequest("invalid");

            mockMvc.perform(post("/api/v1/validate/vpa")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("✅ GET - Quick VPA validation")
        void quickVpaValidation_shouldWork() throws Exception {
            mockMvc.perform(get("/api/v1/validate/vpa/{vpa}", "merchant@hdfc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.bankHandle").value("hdfc"));
        }
    }

    // ========================================================================
    // HEALTH ENDPOINT
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/health - Health Check")
    class HealthEndpointTests {

        @Test
        @DisplayName("✅ 200 - Health check should return UP")
        void healthCheck_shouldReturnUp() throws Exception {
            mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").exists());
        }
    }

    // ========================================================================
    // ERROR HANDLING
    // ========================================================================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("❌ Invalid JSON should return 400")
        void invalidJson_shouldReturn400() throws Exception {
            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ Empty body should return 400")
        void emptyBody_shouldReturn400() throws Exception {
            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ Wrong content type should return 415")
        void wrongContentType_shouldReturn415() throws Exception {
            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("test"))
                .andExpect(status().isUnsupportedMediaType());
        }
    }

    // ========================================================================
    // RESPONSE STRUCTURE
    // ========================================================================

    @Nested
    @DisplayName("Response Structure Validation")
    class ResponseStructureTests {

        @Test
        @DisplayName("✅ Success response should have all required fields")
        void successResponse_shouldHaveAllFields() throws Exception {
            TransferRequest request = createValidRequest();

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionRef").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.charges").exists())
                .andExpect(jsonPath("$.payerVpa").exists())
                .andExpect(jsonPath("$.payeeVpa").exists());
        }

        @Test
        @DisplayName("❌ Error response should have standard structure")
        void errorResponse_shouldHaveStandardStructure() throws Exception {
            TransferRequest request = createValidRequest();
            request.setPayerVpa(null);

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private TransferRequest createValidRequest() {
        TransferRequest request = new TransferRequest();
        request.setPayerVpa("payer@sbi");
        request.setPayeeVpa("payee@hdfc");
        request.setAmount(new BigDecimal("1000.00"));
        request.setTransactionType("P2P");
        return request;
    }
}