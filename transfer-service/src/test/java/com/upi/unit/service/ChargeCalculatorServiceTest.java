package com.upi.unit.service;

import com.upi.service.ChargeCalculatorService;
import com.upi.service.ChargeCalculatorService.ChargeResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for ChargeCalculatorService
 * 
 * TEST PYRAMID: UNIT TESTS (70%)
 * - No dependencies, pure logic testing
 */
@DisplayName("ChargeCalculatorService - Unit Tests")
class ChargeCalculatorServiceTest {

    private ChargeCalculatorService chargeCalculator;

    @BeforeEach
    void setUp() {
        chargeCalculator = new ChargeCalculatorService();
    }

    // ========================================================================
    // P2P TRANSACTIONS (FREE)
    // ========================================================================

    @Nested
    @DisplayName("P2P Transactions - Should Be Free")
    class P2PTransactionTests {

        @ParameterizedTest(name = "P2P amount ₹{0} should have zero charges")
        @DisplayName("✅ P2P transactions should be free")
        @ValueSource(strings = {"1", "100", "1000", "10000", "100000"})
        void p2pTransactions_shouldBeFree(String amount) {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal(amount), "P2P", false);

            assertAll("P2P charges",
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getBaseFee())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getGst())),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()))
            );
        }

        @Test
        @DisplayName("✅ P2P net amount should equal original amount")
        void p2pNetAmount_shouldEqualOriginalAmount() {
            BigDecimal amount = new BigDecimal("5000");
            ChargeResult result = chargeCalculator.calculateCharges(amount, "P2P", false);

            assertEquals(0, amount.compareTo(result.getNetAmount()));
        }

        @Test
        @DisplayName("✅ UPI type should also be free")
        void upiType_shouldBeFree() {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("1000"), "UPI", false);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()));
        }

        @Test
        @DisplayName("✅ UPI_LITE type should be free")
        void upiLiteType_shouldBeFree() {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("500"), "UPI_LITE", false);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()));
        }
    }

    // ========================================================================
    // P2M TRANSACTIONS (0.30% FEE)
    // ========================================================================

    @Nested
    @DisplayName("P2M Transactions - 0.30% Fee")
    class P2MTransactionTests {

        @Test
        @DisplayName("✅ P2M should have 0.30% base fee")
        void p2mTransactions_shouldHave030PercentFee() {
            BigDecimal amount = new BigDecimal("10000");
            ChargeResult result = chargeCalculator.calculateCharges(amount, "P2M", false);

            // 0.30% of 10000 = 30
            assertEquals(0, new BigDecimal("30.00").compareTo(result.getBaseFee()));
        }

        @ParameterizedTest(name = "P2M ₹{0} should have base fee ₹{1}")
        @DisplayName("✅ P2M fee calculations")
        @CsvSource({
            "1000, 3.00",
            "5000, 15.00",
            "10000, 30.00",
            "50000, 150.00"
        })
        void p2mFeeCalculations(String amount, String expectedFee) {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal(amount), "P2M", false);

            assertEquals(0, new BigDecimal(expectedFee).compareTo(result.getBaseFee()),
                String.format("Expected fee %s for amount %s", expectedFee, amount));
        }

        @Test
        @DisplayName("✅ MERCHANT type should have same fee as P2M")
        void merchantType_shouldHaveSameFeeAsP2M() {
            BigDecimal amount = new BigDecimal("10000");
            ChargeResult p2mResult = chargeCalculator.calculateCharges(amount, "P2M", false);
            ChargeResult merchantResult = chargeCalculator.calculateCharges(amount, "MERCHANT", false);

            assertEquals(0, p2mResult.getBaseFee().compareTo(merchantResult.getBaseFee()));
        }
    }

    // ========================================================================
    // BILL TRANSACTIONS (0.50% FEE)
    // ========================================================================

    @Nested
    @DisplayName("BILL Transactions - 0.50% Fee")
    class BillTransactionTests {

        @Test
        @DisplayName("✅ BILL should have 0.50% base fee")
        void billTransactions_shouldHave050PercentFee() {
            BigDecimal amount = new BigDecimal("10000");
            ChargeResult result = chargeCalculator.calculateCharges(amount, "BILL", false);

            // 0.50% of 10000 = 50
            assertEquals(0, new BigDecimal("50.00").compareTo(result.getBaseFee()));
        }

        @ParameterizedTest(name = "BILL ₹{0} should have base fee ₹{1}")
        @DisplayName("✅ BILL fee calculations")
        @CsvSource({
            "1000, 5.00",
            "5000, 25.00",
            "10000, 50.00",
            "20000, 100.00"
        })
        void billFeeCalculations(String amount, String expectedFee) {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal(amount), "BILL", false);

            assertEquals(0, new BigDecimal(expectedFee).compareTo(result.getBaseFee()),
                String.format("Expected fee %s for amount %s", expectedFee, amount));
        }
    }

    // ========================================================================
    // GST CALCULATION (18%)
    // ========================================================================

    @Nested
    @DisplayName("GST Calculation - 18%")
    class GSTCalculationTests {

        @Test
        @DisplayName("✅ GST should be 18% of base fee")
        void gst_shouldBe18PercentOfBaseFee() {
            // P2M with 10000 amount = 30 base fee
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("10000"), "P2M", false);

            // GST = 18% of 30 = 5.40
            assertEquals(0, new BigDecimal("5.40").compareTo(result.getGst()));
        }

        @Test
        @DisplayName("✅ GST should be zero for free transactions")
        void gst_shouldBeZeroForFreeTransactions() {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("1000"), "P2P", false);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getGst()));
        }

        @Test
        @DisplayName("✅ calculateGST method should work correctly")
        void calculateGSTMethod_shouldWork() {
            BigDecimal gst = chargeCalculator.calculateGST(new BigDecimal("100"));
            assertEquals(0, new BigDecimal("18.00").compareTo(gst));
        }

        @Test
        @DisplayName("✅ GST on zero base fee should be zero")
        void gstOnZeroBaseFee_shouldBeZero() {
            BigDecimal gst = chargeCalculator.calculateGST(BigDecimal.ZERO);
            assertEquals(0, BigDecimal.ZERO.compareTo(gst));
        }

        @Test
        @DisplayName("✅ GST on null should be zero")
        void gstOnNull_shouldBeZero() {
            BigDecimal gst = chargeCalculator.calculateGST(null);
            assertEquals(0, BigDecimal.ZERO.compareTo(gst));
        }
    }

    // ========================================================================
    // TOTAL CHARGES & NET AMOUNT
    // ========================================================================

    @Nested
    @DisplayName("Total Charges & Net Amount")
    class TotalChargesTests {

        @Test
        @DisplayName("✅ Total charges = base fee + GST")
        void totalCharges_shouldEqualBaseFeesPlusGST() {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("10000"), "P2M", false);

            // Base fee: 30, GST: 5.40, Total: 35.40
            BigDecimal expectedTotal = result.getBaseFee().add(result.getGst());
            assertEquals(0, expectedTotal.compareTo(result.getTotalCharges()));
        }

        @Test
        @DisplayName("✅ Net amount = amount + total charges")
        void netAmount_shouldEqualAmountPlusTotalCharges() {
            BigDecimal amount = new BigDecimal("10000");
            ChargeResult result = chargeCalculator.calculateCharges(amount, "P2M", false);

            BigDecimal expectedNet = amount.add(result.getTotalCharges());
            assertEquals(0, expectedNet.compareTo(result.getNetAmount()));
        }
    }

    // ========================================================================
    // INTER-BANK TRANSACTIONS
    // ========================================================================

    @Nested
    @DisplayName("Inter-Bank Transaction Fees")
    class InterBankTests {

        @Test
        @DisplayName("✅ Inter-bank P2M should add ₹2 fee")
        void interBankP2M_shouldAddFee() {
            ChargeResult sameBankResult = chargeCalculator.calculateCharges(
                new BigDecimal("10000"), "P2M", false);
            ChargeResult interBankResult = chargeCalculator.calculateCharges(
                new BigDecimal("10000"), "P2M", true);

            // Inter-bank should have ₹2 more base fee
            BigDecimal difference = interBankResult.getBaseFee()
                .subtract(sameBankResult.getBaseFee());
            assertEquals(0, new BigDecimal("2.00").compareTo(difference));
        }

        @Test
        @DisplayName("✅ Inter-bank P2P should still be free")
        void interBankP2P_shouldStillBeFree() {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("10000"), "P2P", true);

            // P2P is free, so no inter-bank fee added
            assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()));
        }
    }

    // ========================================================================
    // EDGE CASES
    // ========================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("✅ Null amount should return zero charges")
        void nullAmount_shouldReturnZeroCharges() {
            ChargeResult result = chargeCalculator.calculateCharges(null, "P2M", false);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()));
        }

        @Test
        @DisplayName("✅ Zero amount should return zero charges")
        void zeroAmount_shouldReturnZeroCharges() {
            ChargeResult result = chargeCalculator.calculateCharges(
                BigDecimal.ZERO, "P2M", false);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()));
        }

        @Test
        @DisplayName("✅ Negative amount should return zero charges")
        void negativeAmount_shouldReturnZeroCharges() {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("-1000"), "P2M", false);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()));
        }

        @Test
        @DisplayName("✅ Null transaction type should default to P2P (free)")
        void nullTransactionType_shouldDefaultToP2P() {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("1000"), null, false);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()));
        }

        @Test
        @DisplayName("✅ Unknown transaction type should default to P2P (free)")
        void unknownTransactionType_shouldDefaultToP2P() {
            ChargeResult result = chargeCalculator.calculateCharges(
                new BigDecimal("1000"), "UNKNOWN_TYPE", false);

            assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalCharges()));
        }
    }

    // ========================================================================
    // IS TRANSACTION FREE
    // ========================================================================

    @Nested
    @DisplayName("isTransactionFree Method")
    class IsTransactionFreeTests {

        @ParameterizedTest(name = "Type {0} should be free")
        @DisplayName("✅ Free transaction types")
        @ValueSource(strings = {"P2P", "UPI", "UPI_LITE", "p2p", "upi"})
        void freeTransactionTypes(String type) {
            assertTrue(chargeCalculator.isTransactionFree(type));
        }

        @ParameterizedTest(name = "Type {0} should have charges")
        @DisplayName("❌ Non-free transaction types")
        @ValueSource(strings = {"P2M", "MERCHANT", "BILL", "BILLPAY"})
        void nonFreeTransactionTypes(String type) {
            assertFalse(chargeCalculator.isTransactionFree(type));
        }

        @Test
        @DisplayName("✅ Null type should be free (defaults to P2P)")
        void nullType_shouldBeFree() {
            assertTrue(chargeCalculator.isTransactionFree(null));
        }
    }

    // ========================================================================
    // CHARGE RESULT HELPER METHODS
    // ========================================================================

    @Nested
    @DisplayName("ChargeResult Helper Methods")
    class ChargeResultHelperTests {

        @Test
        @DisplayName("✅ isFreeTransaction should work correctly")
        void isFreeTransaction_shouldWork() {
            ChargeResult freeResult = chargeCalculator.calculateCharges(
                new BigDecimal("1000"), "P2P", false);
            ChargeResult paidResult = chargeCalculator.calculateCharges(
                new BigDecimal("1000"), "P2M", false);

            assertTrue(freeResult.isFreeTransaction());
            assertFalse(paidResult.isFreeTransaction());
        }
    }
}
