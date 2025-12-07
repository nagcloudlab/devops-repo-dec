package com.upi.integration;

import com.upi.dto.TransferRequest;
import com.upi.dto.TransferResponse;
import com.upi.entity.Transaction;
import com.upi.entity.Transaction.TransactionStatus;
import com.upi.exception.PaymentException;
import com.upi.repository.TransactionRepository;
import com.upi.service.TransferService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests - Tests with full Spring context
 * 
 * TEST PYRAMID: INTEGRATION TESTS (20%)
 * - Full Spring context loaded
 * - Real database (H2)
 * - Tests component integration
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Integration Tests - Service Layer")
@Transactional
class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    // ========================================================================
    // END-TO-END TRANSFER FLOW
    // ========================================================================

    @Nested
    @DisplayName("Complete Transfer Flow")
    class CompleteTransferFlowTests {

        @Test
        @DisplayName("✅ Complete P2P transfer flow should work")
        void completeP2PTransfer_shouldWork() {
            // Arrange
            TransferRequest request = createTransferRequest("payer@sbi", "payee@hdfc", "1000");

            // Act
            TransferResponse response = transferService.processTransfer(request);

            // Assert
            assertAll("Transfer response",
                () -> assertNotNull(response.getTransactionRef()),
                () -> assertEquals(TransactionStatus.SUCCESS, response.getStatus()),
                () -> assertNotNull(response.getBankRrn())
            );

            // Verify persisted in database
            Transaction saved = transactionRepository
                .findByTransactionRef(response.getTransactionRef())
                .orElseThrow();

            assertAll("Persisted transaction",
                () -> assertEquals(TransactionStatus.SUCCESS, saved.getStatus()),
                () -> assertEquals(0, new BigDecimal("1000").compareTo(saved.getAmount())),
                () -> assertEquals("payer@sbi", saved.getPayerVpa()),
                () -> assertEquals("payee@hdfc", saved.getPayeeVpa())
            );
        }

        @Test
        @DisplayName("✅ Multiple transfers should create separate transactions")
        void multipleTransfers_shouldCreateSeparateTransactions() {
            // Arrange & Act
            TransferResponse response1 = transferService.processTransfer(
                createTransferRequest("user1@sbi", "merchant@hdfc", "500"));
            TransferResponse response2 = transferService.processTransfer(
                createTransferRequest("user2@icici", "merchant@hdfc", "1000"));

            // Assert
            assertNotEquals(response1.getTransactionRef(), response2.getTransactionRef());
            assertEquals(2, transactionRepository.count());
        }

        @Test
        @DisplayName("✅ Transfer should calculate and persist charges")
        void transfer_shouldPersistCharges() {
            // Arrange
            TransferRequest request = createTransferRequest("payer@sbi", "payee@hdfc", "1000");
            request.setTransactionType("P2M");

            // Act
            TransferResponse response = transferService.processTransfer(request);

            // Assert
            assertNotNull(response.getCharges());

            Transaction saved = transactionRepository
                .findByTransactionRef(response.getTransactionRef())
                .orElseThrow();
            assertNotNull(saved.getCharges());
        }
    }

    // ========================================================================
    // TRANSACTION STATUS RETRIEVAL
    // ========================================================================

    @Nested
    @DisplayName("Transaction Status Retrieval")
    class TransactionStatusTests {

        @Test
        @DisplayName("✅ Should retrieve status of completed transaction")
        void shouldRetrieveCompletedTransactionStatus() {
            // Arrange - Create transaction
            TransferRequest request = createTransferRequest("payer@sbi", "payee@hdfc", "1000");
            TransferResponse createResponse = transferService.processTransfer(request);

            // Act - Retrieve status
            TransferResponse statusResponse = transferService.getTransactionStatus(
                createResponse.getTransactionRef());

            // Assert
            assertAll("Status response",
                () -> assertEquals(createResponse.getTransactionRef(), statusResponse.getTransactionRef()),
                () -> assertEquals(TransactionStatus.SUCCESS, statusResponse.getStatus()),
                () -> assertEquals("payer@sbi", statusResponse.getPayerVpa())
            );
        }

        @Test
        @DisplayName("❌ Should throw exception for non-existent transaction")
        void shouldThrowForNonExistentTransaction() {
            assertThrows(PaymentException.class,
                () -> transferService.getTransactionStatus("NON_EXISTENT_REF"));
        }
    }

    // ========================================================================
    // TRANSACTION HISTORY
    // ========================================================================

    @Nested
    @DisplayName("Transaction History")
    class TransactionHistoryTests {

        @Test
        @DisplayName("✅ Should retrieve transaction history for VPA")
        void shouldRetrieveTransactionHistory() {
            // Arrange - Create multiple transactions for same payer
            String payerVpa = "history.user@sbi";
            for (int i = 0; i < 5; i++) {
                transferService.processTransfer(
                    createTransferRequest(payerVpa, "payee" + i + "@hdfc", "100"));
            }

            // Act
            List<Transaction> history = transferService.getTransactionHistory(payerVpa);

            // Assert
            assertEquals(5, history.size());
            assertTrue(history.stream().allMatch(t -> t.getPayerVpa().equals(payerVpa)));
        }

        @Test
        @DisplayName("✅ History should be empty for new VPA")
        void historyForNewVPA_shouldBeEmpty() {
            List<Transaction> history = transferService.getTransactionHistory("new.user@sbi");
            assertTrue(history.isEmpty());
        }
    }

    // ========================================================================
    // DATABASE INTEGRATION
    // ========================================================================

    @Nested
    @DisplayName("Database Integration")
    class DatabaseIntegrationTests {

        @Test
        @DisplayName("✅ Transaction should be persisted with all fields")
        void transactionPersistence_shouldIncludeAllFields() {
            // Arrange
            TransferRequest request = createTransferRequest("payer@sbi", "payee@hdfc", "5000");
            request.setRemarks("Integration test transfer");
            request.setTransactionType("P2P");

            // Act
            TransferResponse response = transferService.processTransfer(request);

            // Assert
            Transaction saved = transactionRepository
                .findByTransactionRef(response.getTransactionRef())
                .orElseThrow();

            assertAll("Persisted fields",
                () -> assertNotNull(saved.getId()),
                () -> assertNotNull(saved.getTransactionRef()),
                () -> assertEquals("payer@sbi", saved.getPayerVpa()),
                () -> assertEquals("payee@hdfc", saved.getPayeeVpa()),
                () -> assertEquals(0, new BigDecimal("5000").compareTo(saved.getAmount())),
                () -> assertEquals("P2P", saved.getTransactionType()),
                () -> assertEquals("Integration test transfer", saved.getRemarks()),
                () -> assertNotNull(saved.getCreatedAt()),
                () -> assertNotNull(saved.getCompletedAt())
            );
        }

        @Test
        @DisplayName("✅ Should find transaction by reference")
        void shouldFindByReference() {
            // Arrange
            TransferResponse response = transferService.processTransfer(
                createTransferRequest("finder@sbi", "merchant@hdfc", "100"));

            // Act
            boolean exists = transactionRepository.existsByTransactionRef(
                response.getTransactionRef());

            // Assert
            assertTrue(exists);
        }

        @Test
        @DisplayName("✅ Should count transactions by status")
        void shouldCountByStatus() {
            // Arrange - Create transactions
            for (int i = 0; i < 3; i++) {
                transferService.processTransfer(
                    createTransferRequest("user" + i + "@sbi", "merchant@hdfc", "100"));
            }

            // Act
            long successCount = transactionRepository.countByStatus(TransactionStatus.SUCCESS);

            // Assert
            assertEquals(3, successCount);
        }
    }

    // ========================================================================
    // CONCURRENT OPERATIONS
    // ========================================================================

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperationTests {

        @Test
        @DisplayName("✅ Concurrent transfers should generate unique references")
        void concurrentTransfers_shouldGenerateUniqueRefs() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            String[] refs = new String[threadCount];
            Thread[] threads = new Thread[threadCount];

            // Act
            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    TransferResponse response = transferService.processTransfer(
                        createTransferRequest("user" + idx + "@sbi", "merchant@hdfc", "100"));
                    refs[idx] = response.getTransactionRef();
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            // Assert - All refs should be unique
            long uniqueCount = java.util.Arrays.stream(refs)
                .filter(r -> r != null)
                .distinct()
                .count();

            assertEquals(threadCount, uniqueCount, 
                "All transaction references should be unique");
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private TransferRequest createTransferRequest(String payer, String payee, String amount) {
        TransferRequest request = new TransferRequest();
        request.setPayerVpa(payer);
        request.setPayeeVpa(payee);
        request.setAmount(new BigDecimal(amount));
        request.setTransactionType("P2P");
        return request;
    }
}
