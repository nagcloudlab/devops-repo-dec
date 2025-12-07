package com.upi.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upi.dto.TransferRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E Tests - Complete User Journey Tests
 * 
 * TEST PYRAMID: E2E TESTS (2%)
 * - Tests complete user workflows
 * - Simulates real user interactions
 * - Most expensive to run
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("E2E Tests - Complete User Journeys")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserJourneyE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================================================
    // JOURNEY 1: COMPLETE FUND TRANSFER WORKFLOW
    // ========================================================================

    @Nested
    @DisplayName("Journey: Complete Fund Transfer")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CompleteFundTransferJourney {

        private static String transactionRef;

        @Test
        @Order(1)
        @DisplayName("Step 1: Validate payer VPA")
        void step1_validatePayerVpa() throws Exception {
            mockMvc.perform(get("/api/v1/validate/vpa/{vpa}", "sender@sbi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.bankHandle").value("sbi"));
        }

        @Test
        @Order(2)
        @DisplayName("Step 2: Validate payee VPA")
        void step2_validatePayeeVpa() throws Exception {
            mockMvc.perform(get("/api/v1/validate/vpa/{vpa}", "receiver@hdfc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.bankHandle").value("hdfc"));
        }

        @Test
        @Order(3)
        @DisplayName("Step 3: Initiate fund transfer")
        void step3_initiateTransfer() throws Exception {
            TransferRequest request = new TransferRequest();
            request.setPayerVpa("sender@sbi");
            request.setPayeeVpa("receiver@hdfc");
            request.setAmount(new BigDecimal("5000.00"));
            request.setTransactionType("P2P");
            request.setRemarks("E2E test payment");

            MvcResult result = mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionRef").exists())
                .andReturn();

            // Store transaction ref for next steps
            JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString());
            transactionRef = response.get("transactionRef").asText();
            
            assertNotNull(transactionRef);
            assertTrue(transactionRef.startsWith("TXN"));
        }

        @Test
        @Order(4)
        @DisplayName("Step 4: Check transaction status")
        void step4_checkTransactionStatus() throws Exception {
            assertNotNull(transactionRef, "Transaction ref should be set from previous step");

            mockMvc.perform(get("/api/v1/transfer/{ref}", transactionRef))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionRef").value(transactionRef))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.payerVpa").value("sender@sbi"))
                .andExpect(jsonPath("$.payeeVpa").value("receiver@hdfc"));
        }

        @Test
        @Order(5)
        @DisplayName("Step 5: Verify in transaction history")
        void step5_verifyInHistory() throws Exception {
            mockMvc.perform(get("/api/v1/transfer/history/{vpa}", "sender@sbi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payerVpa").value("sender@sbi"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
        }
    }

    // ========================================================================
    // JOURNEY 2: MERCHANT PAYMENT WITH CHARGES
    // ========================================================================

    @Nested
    @DisplayName("Journey: Merchant Payment with Charges")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MerchantPaymentJourney {

        @Test
        @Order(1)
        @DisplayName("Step 1: P2M transfer should include charges")
        void step1_p2mTransferWithCharges() throws Exception {
            TransferRequest request = new TransferRequest();
            request.setPayerVpa("customer@icici");
            request.setPayeeVpa("shopkeeper@axis");
            request.setAmount(new BigDecimal("10000.00"));
            request.setTransactionType("P2M");
            request.setRemarks("Shop purchase");

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.charges").exists())
                .andExpect(jsonPath("$.totalAmount").exists());
        }
    }

    // ========================================================================
    // JOURNEY 3: FAILED TRANSFER RECOVERY
    // ========================================================================

    @Nested
    @DisplayName("Journey: Handle Invalid Transfer Attempts")
    class FailedTransferJourney {

        @Test
        @DisplayName("Attempt transfer with invalid VPA - should get clear error")
        void attemptInvalidTransfer_shouldGetClearError() throws Exception {
            TransferRequest request = new TransferRequest();
            request.setPayerVpa("invalid-vpa");
            request.setPayeeVpa("receiver@hdfc");
            request.setAmount(new BigDecimal("1000.00"));

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Attempt transfer exceeding limit - should get clear error")
        void attemptExceedingLimit_shouldGetClearError() throws Exception {
            TransferRequest request = new TransferRequest();
            request.setPayerVpa("user@sbi");
            request.setPayeeVpa("merchant@hdfc");
            request.setAmount(new BigDecimal("500000.00")); // Exceeds limit

            mockMvc.perform(post("/api/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("amount"));
        }
    }

    // ========================================================================
    // JOURNEY 4: MULTIPLE TRANSACTIONS
    // ========================================================================

    @Nested
    @DisplayName("Journey: Multiple Sequential Transactions")
    class MultipleTransactionsJourney {

        @Test
        @DisplayName("Multiple transfers from same payer should all succeed")
        void multipleTransfers_shouldAllSucceed() throws Exception {
            String payerVpa = "bulk.sender@sbi";

            for (int i = 1; i <= 5; i++) {
                TransferRequest request = new TransferRequest();
                request.setPayerVpa(payerVpa);
                request.setPayeeVpa("recipient" + i + "@hdfc");
                request.setAmount(new BigDecimal("100.00"));
                request.setTransactionType("P2P");
                request.setRemarks("Bulk payment " + i);

                mockMvc.perform(post("/api/v1/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SUCCESS"));
            }

            // Verify all in history
            mockMvc.perform(get("/api/v1/transfer/history/{vpa}", payerVpa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
        }
    }

    // ========================================================================
    // JOURNEY 5: API HEALTH & AVAILABILITY
    // ========================================================================

    @Nested
    @DisplayName("Journey: System Health Check")
    class SystemHealthJourney {

        @Test
        @DisplayName("All endpoints should be accessible")
        void allEndpoints_shouldBeAccessible() throws Exception {
            // Health endpoint
            mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

            // Actuator health
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

            // Validation endpoint
            mockMvc.perform(get("/api/v1/validate/vpa/test@sbi"))
                .andExpect(status().isOk());
        }
    }
}
