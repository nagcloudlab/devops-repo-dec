package com.npci.upi.integration;

import com.npci.upi.service.UPITransferService;
import com.npci.upi.model.TransferRequest;
import com.npci.upi.model.TransferResponse;
import com.npci.upi.exception.PaymentException;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * INTEGRATION TESTS - UPI Transfer Service
 * ============================================================================
 * 
 * PURPOSE:
 * Test the service with simulated external dependencies:
 * - Database operations (transaction logging)
 * - Concurrent transaction handling
 * - End-to-end flow validation
 * 
 * RUNS: After unit tests pass, takes 5-10 minutes
 * 
 * IN REAL SCENARIO, these would use:
 * - @Testcontainers for database
 * - WireMock for external API mocking
 * - Embedded Kafka for message queue testing
 * 
 * ============================================================================
 */
@DisplayName("UPI Transfer Service - Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
class UPITransferIntegrationTest {

    private UPITransferService service;

    @BeforeEach
    void setUp() {
        service = new UPITransferService();
    }

    // ========================================================================
    // END-TO-END TRANSFER FLOW TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("End-to-End Transfer Flow")
    class E2EFlowTests {

        @Test
        @Order(1)
        @DisplayName("✅ Complete P2P transfer flow")
        void testCompleteP2PFlow() throws PaymentException {
            // Step 1: Create transfer request
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("customer@okicici")
                    .payeeVpa("friend@ybl")
                    .amount(new BigDecimal("1500.00"))
                    .transactionType("P2P")
                    .remarks("Birthday gift")
                    .build();

            // Step 2: Process transfer
            TransferResponse response = service.processTransfer(request);

            // Step 3: Verify response
            assertAll("Transfer Response Validation",
                () -> assertNotNull(response.getTransactionRef()),
                () -> assertEquals("SUCCESS", response.getStatus()),
                () -> assertEquals("00", response.getStatusCode()),
                () -> assertEquals("customer@okicici", response.getPayerVpa()),
                () -> assertEquals("friend@ybl", response.getPayeeVpa()),
                () -> assertNotNull(response.getTimestamp())
            );

            // Step 4: Verify transaction status check works
            String status = service.checkTransactionStatus(response.getTransactionRef());
            assertEquals("SUCCESS", status);
        }

        @Test
        @Order(2)
        @DisplayName("✅ Complete P2M (Merchant) transfer flow with charges")
        void testCompleteP2MFlow() throws PaymentException {
            // Merchant payment above ₹2000 should have charges
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("shopper@okicici")
                    .payeeVpa("bigbazaar@ybl")
                    .amount(new BigDecimal("5000.00"))
                    .transactionType("P2M")
                    .remarks("Grocery shopping")
                    .build();

            // Process transfer
            TransferResponse response = service.processTransfer(request);
            assertEquals("SUCCESS", response.getStatus());

            // Verify charges calculation
            BigDecimal charges = service.calculateCharges(
                request.getAmount(), 
                request.getTransactionType()
            );
            assertEquals(new BigDecimal("15.00"), charges); // 5000 * 0.3%
        }
    }

    // ========================================================================
    // CONCURRENT TRANSACTION TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("Concurrent Transaction Handling")
    class ConcurrencyTests {

        @Test
        @Order(1)
        @DisplayName("✅ Multiple concurrent transfers should all succeed")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void testConcurrentTransfers() throws InterruptedException, ExecutionException {
            int numberOfTransactions = 10;
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<Future<TransferResponse>> futures = new ArrayList<>();

            // Submit concurrent transfer requests
            for (int i = 0; i < numberOfTransactions; i++) {
                final int index = i;
                futures.add(executor.submit(() -> {
                    TransferRequest request = TransferRequest.builder()
                            .payerVpa("user" + index + "@okicici")
                            .payeeVpa("merchant@ybl")
                            .amount(new BigDecimal("100.00"))
                            .build();
                    return service.processTransfer(request);
                }));
            }

            // Verify all transactions succeeded
            List<String> transactionRefs = new ArrayList<>();
            for (Future<TransferResponse> future : futures) {
                TransferResponse response = future.get();
                assertEquals("SUCCESS", response.getStatus());
                transactionRefs.add(response.getTransactionRef());
            }

            // Verify all transaction refs are unique
            long uniqueCount = transactionRefs.stream().distinct().count();
            assertEquals(numberOfTransactions, uniqueCount, 
                "All transaction references should be unique");

            executor.shutdown();
        }

        @Test
        @Order(2)
        @DisplayName("✅ High volume transaction processing")
        @Timeout(value = 60, unit = TimeUnit.SECONDS)
        void testHighVolumeTransactions() throws InterruptedException {
            int totalTransactions = 50;
            CountDownLatch latch = new CountDownLatch(totalTransactions);
            List<String> results = new CopyOnWriteArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(10);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < totalTransactions; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        TransferRequest request = TransferRequest.builder()
                                .payerVpa("bulk" + index + "@okicici")
                                .payeeVpa("collector@ybl")
                                .amount(new BigDecimal("50.00"))
                                .build();
                        TransferResponse response = service.processTransfer(request);
                        results.add(response.getStatus());
                    } catch (Exception e) {
                        results.add("FAILED");
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            long endTime = System.currentTimeMillis();

            executor.shutdown();

            // Verify results
            long successCount = results.stream().filter("SUCCESS"::equals).count();
            assertEquals(totalTransactions, successCount, 
                "All transactions should succeed");

            // Log performance metrics
            System.out.println("=== Performance Metrics ===");
            System.out.println("Total Transactions: " + totalTransactions);
            System.out.println("Time Taken: " + (endTime - startTime) + "ms");
            System.out.println("TPS: " + (totalTransactions * 1000.0 / (endTime - startTime)));
        }
    }

    // ========================================================================
    // BANK SWITCH SIMULATION TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("Multi-Bank Transfer Scenarios")
    class MultiBankTests {

        @Test
        @Order(1)
        @DisplayName("✅ Cross-bank transfer (ICICI to HDFC)")
        void testCrossBankTransfer() throws PaymentException {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("sender@okicici")
                    .payeeVpa("receiver@hdfcbank")
                    .amount(new BigDecimal("2500.00"))
                    .build();

            TransferResponse response = service.processTransfer(request);
            
            assertEquals("SUCCESS", response.getStatus());
        }

        @Test
        @Order(2)
        @DisplayName("✅ Same-bank transfer (SBI to SBI)")
        void testSameBankTransfer() throws PaymentException {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("account1@sbi")
                    .payeeVpa("account2@sbi")
                    .amount(new BigDecimal("1000.00"))
                    .build();

            TransferResponse response = service.processTransfer(request);
            
            assertEquals("SUCCESS", response.getStatus());
        }

        @Test
        @Order(3)
        @DisplayName("✅ Transfer to payment apps (PhonePe, GPay, Paytm)")
        void testPaymentAppTransfers() throws PaymentException {
            String[] paymentApps = {"@ybl", "@okaxis", "@paytm", "@ibl"};
            
            for (String app : paymentApps) {
                TransferRequest request = TransferRequest.builder()
                        .payerVpa("user@okicici")
                        .payeeVpa("merchant" + app)
                        .amount(new BigDecimal("299.00"))
                        .build();

                TransferResponse response = service.processTransfer(request);
                
                assertEquals("SUCCESS", response.getStatus(), 
                    "Transfer to " + app + " should succeed");
            }
        }
    }

    // ========================================================================
    // EDGE CASE & BOUNDARY TESTS
    // ========================================================================
    
    @Nested
    @DisplayName("Edge Cases & Boundary Conditions")
    class EdgeCaseTests {

        @Test
        @Order(1)
        @DisplayName("✅ Transfer with exact minimum amount boundary")
        void testExactMinimumAmount() throws PaymentException {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@ybl")
                    .amount(new BigDecimal("1.00"))
                    .build();

            TransferResponse response = service.processTransfer(request);
            assertEquals("SUCCESS", response.getStatus());
        }

        @Test
        @Order(2)
        @DisplayName("✅ Transfer with exact maximum amount boundary")
        void testExactMaximumAmount() throws PaymentException {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("corporate@okicici")
                    .payeeVpa("vendor@ybl")
                    .amount(new BigDecimal("100000.00"))
                    .build();

            TransferResponse response = service.processTransfer(request);
            assertEquals("SUCCESS", response.getStatus());
        }

        @Test
        @Order(3)
        @DisplayName("✅ Transfer with decimal precision")
        void testDecimalPrecision() throws PaymentException {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@ybl")
                    .amount(new BigDecimal("99.99"))
                    .build();

            TransferResponse response = service.processTransfer(request);
            
            assertEquals("SUCCESS", response.getStatus());
            assertEquals(new BigDecimal("99.99"), response.getAmount());
        }
    }
}
