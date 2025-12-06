package com.npci.upi.flaky;

import com.npci.upi.service.UPITransferService;
import com.npci.upi.model.TransferRequest;
import com.npci.upi.model.TransferResponse;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * FLAKY TEST EXAMPLES - For Training Demonstration
 * ============================================================================
 * 
 * This file contains:
 * 1. FLAKY tests (marked with ❌ FLAKY) - demonstrate common flakiness causes
 * 2. FIXED versions (marked with ✅ FIXED) - show the correct approach
 * 
 * COMMON CAUSES OF FLAKY TESTS:
 * - Time-dependent assertions
 * - Random data without seeding
 * - Race conditions
 * - External dependency assumptions
 * - Order-dependent tests
 * - Shared mutable state
 * 
 * USE THIS FILE TO:
 * 1. Show TMs what flaky tests look like
 * 2. Run multiple times to see intermittent failures
 * 3. Compare with fixed versions
 * 
 * ============================================================================
 */
@DisplayName("Flaky Test Examples - Training Demo")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlakyTestExamples {

    private UPITransferService service;
    
    // Shared state - common cause of flakiness!
    private static int sharedCounter = 0;

    @BeforeEach
    void setUp() {
        service = new UPITransferService();
    }

    // ========================================================================
    // EXAMPLE 1: TIME-DEPENDENT FLAKINESS
    // ========================================================================
    
    @Nested
    @DisplayName("Example 1: Time-Dependent Tests")
    class TimeDependentTests {

        /**
         * ❌ FLAKY: This test depends on exact millisecond timing
         * Will fail intermittently when system is under load
         */
        @Test
        @Order(1)
        @DisplayName("❌ FLAKY: Exact timestamp matching")
        @Disabled("Intentionally flaky - enable to demonstrate")
        void flakyTimestampTest() throws Exception {
            LocalDateTime beforeCall = LocalDateTime.now();
            
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@ybl")
                    .amount(new BigDecimal("100"))
                    .build();
            
            TransferResponse response = service.processTransfer(request);
            
            // ❌ FLAKY: Exact second matching fails if call spans second boundary
            assertEquals(beforeCall.getSecond(), response.getTimestamp().getSecond(),
                "Timestamp second should match");
        }

        /**
         * ✅ FIXED: Use time range instead of exact matching
         */
        @Test
        @Order(2)
        @DisplayName("✅ FIXED: Time range validation")
        void fixedTimestampTest() throws Exception {
            LocalDateTime beforeCall = LocalDateTime.now();
            
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@ybl")
                    .amount(new BigDecimal("100"))
                    .build();
            
            TransferResponse response = service.processTransfer(request);
            
            LocalDateTime afterCall = LocalDateTime.now();
            
            // ✅ FIXED: Check timestamp is within valid range
            assertTrue(
                !response.getTimestamp().isBefore(beforeCall.minusSeconds(1)) &&
                !response.getTimestamp().isAfter(afterCall.plusSeconds(1)),
                "Timestamp should be within acceptable range"
            );
        }
    }

    // ========================================================================
    // EXAMPLE 2: RANDOM DATA FLAKINESS
    // ========================================================================
    
    @Nested
    @DisplayName("Example 2: Random Data Tests")
    class RandomDataTests {

        /**
         * ❌ FLAKY: Uses unseeded random - different results each run
         */
        @Test
        @Order(1)
        @DisplayName("❌ FLAKY: Unseeded random amount")
        @Disabled("Intentionally flaky - enable to demonstrate")
        void flakyRandomAmountTest() throws Exception {
            Random random = new Random(); // ❌ No seed!
            BigDecimal amount = new BigDecimal(random.nextInt(100000) + 1);
            
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@ybl")
                    .amount(amount)
                    .build();
            
            TransferResponse response = service.processTransfer(request);
            
            // ❌ This assertion is unstable because amount varies
            assertTrue(response.getAmount().compareTo(new BigDecimal("50000")) < 0,
                "Amount should be less than 50000");
        }

        /**
         * ✅ FIXED: Use seeded random for reproducibility
         */
        @Test
        @Order(2)
        @DisplayName("✅ FIXED: Seeded random for reproducibility")
        void fixedRandomAmountTest() throws Exception {
            Random random = new Random(12345); // ✅ Fixed seed!
            BigDecimal amount = new BigDecimal(random.nextInt(50000) + 1); // Also bounded properly
            
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@ybl")
                    .amount(amount)
                    .build();
            
            TransferResponse response = service.processTransfer(request);
            
            // ✅ Deterministic - same seed always produces same value
            assertEquals("SUCCESS", response.getStatus());
            assertNotNull(response.getAmount());
        }

        /**
         * ✅ BETTER: Use parameterized tests instead of random
         */
        @Test
        @Order(3)
        @DisplayName("✅ BETTER: Explicit test cases instead of random")
        void betterExplicitAmountsTest() throws Exception {
            // ✅ BEST PRACTICE: Test specific meaningful values
            BigDecimal[] testAmounts = {
                new BigDecimal("1.00"),      // Minimum
                new BigDecimal("100.00"),    // Common small
                new BigDecimal("5000.00"),   // Common medium
                new BigDecimal("100000.00")  // Maximum
            };
            
            for (BigDecimal amount : testAmounts) {
                TransferRequest request = TransferRequest.builder()
                        .payerVpa("user@okicici")
                        .payeeVpa("shop@ybl")
                        .amount(amount)
                        .build();
                
                TransferResponse response = service.processTransfer(request);
                assertEquals("SUCCESS", response.getStatus(),
                    "Transfer of " + amount + " should succeed");
            }
        }
    }

    // ========================================================================
    // EXAMPLE 3: SHARED STATE FLAKINESS
    // ========================================================================
    
    @Nested
    @DisplayName("Example 3: Shared State Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SharedStateTests {

        /**
         * ❌ FLAKY: Depends on shared static counter
         * Test order or parallel execution causes failures
         */
        @Test
        @Order(1)
        @DisplayName("❌ FLAKY: Shared counter dependency (Test A)")
        @Disabled("Intentionally flaky - enable to demonstrate")
        void flakySharedStateTestA() {
            sharedCounter++; // ❌ Modifies shared state
            assertEquals(1, sharedCounter, "Counter should be 1");
        }

        @Test
        @Order(2)
        @DisplayName("❌ FLAKY: Shared counter dependency (Test B)")
        @Disabled("Intentionally flaky - enable to demonstrate")
        void flakySharedStateTestB() {
            sharedCounter++; // ❌ Depends on Test A running first
            assertEquals(2, sharedCounter, "Counter should be 2");
        }

        /**
         * ✅ FIXED: Each test uses its own isolated state
         */
        @Test
        @Order(3)
        @DisplayName("✅ FIXED: Isolated state per test")
        void fixedIsolatedStateTest() {
            // ✅ Local variable - no shared state
            int localCounter = 0;
            
            localCounter++;
            assertEquals(1, localCounter);
            
            localCounter++;
            assertEquals(2, localCounter);
        }
    }

    // ========================================================================
    // EXAMPLE 4: RACE CONDITION FLAKINESS
    // ========================================================================
    
    @Nested
    @DisplayName("Example 4: Race Condition Tests")
    class RaceConditionTests {

        /**
         * ❌ FLAKY: Race condition with tight timing
         */
        @Test
        @Order(1)
        @DisplayName("❌ FLAKY: Tight timing assumption")
        @Disabled("Intentionally flaky - enable to demonstrate")
        void flakyRaceConditionTest() throws Exception {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@ybl")
                    .amount(new BigDecimal("100"))
                    .build();
            
            long start = System.currentTimeMillis();
            service.processTransfer(request);
            long duration = System.currentTimeMillis() - start;
            
            // ❌ FLAKY: Assumes processing takes less than 50ms
            // Fails under load or on slow CI runners
            assertTrue(duration < 50, 
                "Processing should complete in under 50ms but took " + duration + "ms");
        }

        /**
         * ✅ FIXED: Use reasonable timeout with retry logic
         */
        @Test
        @Order(2)
        @DisplayName("✅ FIXED: Reasonable timeout with buffer")
        void fixedTimeoutTest() throws Exception {
            TransferRequest request = TransferRequest.builder()
                    .payerVpa("user@okicici")
                    .payeeVpa("shop@ybl")
                    .amount(new BigDecimal("100"))
                    .build();
            
            long start = System.currentTimeMillis();
            TransferResponse response = service.processTransfer(request);
            long duration = System.currentTimeMillis() - start;
            
            // ✅ FIXED: Generous timeout that accounts for CI variability
            assertTrue(duration < 5000, 
                "Processing should complete in under 5 seconds");
            assertEquals("SUCCESS", response.getStatus());
        }
    }

    // ========================================================================
    // EXAMPLE 5: FLAKY TEST RETRY STRATEGY
    // ========================================================================
    
    @Nested
    @DisplayName("Example 5: Retry Strategy for Inherently Flaky Operations")
    class RetryStrategyTests {

        /**
         * ✅ PATTERN: Retry mechanism for genuinely flaky external dependencies
         * Use this for: Network calls, database connections, external APIs
         * NOT for: Poorly written tests (fix those instead!)
         */
        @Test
        @Order(1)
        @DisplayName("✅ PATTERN: Retry mechanism for external dependencies")
        void retryPatternTest() throws Exception {
            int maxRetries = 3;
            int retryCount = 0;
            boolean success = false;
            Exception lastException = null;
            
            while (retryCount < maxRetries && !success) {
                try {
                    TransferRequest request = TransferRequest.builder()
                            .payerVpa("user@okicici")
                            .payeeVpa("shop@ybl")
                            .amount(new BigDecimal("100"))
                            .build();
                    
                    TransferResponse response = service.processTransfer(request);
                    
                    if ("SUCCESS".equals(response.getStatus())) {
                        success = true;
                    }
                } catch (Exception e) {
                    lastException = e;
                    retryCount++;
                    Thread.sleep(100 * retryCount); // Exponential backoff
                }
            }
            
            assertTrue(success, 
                "Should succeed within " + maxRetries + " retries. Last error: " + lastException);
        }
    }

    // ========================================================================
    // SUMMARY: FLAKY TEST PREVENTION CHECKLIST
    // ========================================================================
    
    /**
     * FLAKY TEST PREVENTION CHECKLIST:
     * 
     * ✅ DO:
     * - Use time ranges instead of exact timestamps
     * - Seed random number generators
     * - Isolate test state (no shared mutable state)
     * - Use generous timeouts for CI environments
     * - Clean up resources in @AfterEach
     * - Use explicit waits instead of Thread.sleep()
     * - Mock external dependencies
     * 
     * ❌ DON'T:
     * - Depend on test execution order
     * - Use current time in assertions
     * - Share state between tests
     * - Assume fast execution
     * - Leave resources uncleaned
     * - Use unseeded Random
     * - Test against live external services
     */
    @Test
    @DisplayName("📋 Flaky Test Checklist - Always Passes")
    void flakyTestChecklist() {
        // This test documents the checklist and always passes
        assertTrue(true, "Review the checklist in the source code!");
    }
}
