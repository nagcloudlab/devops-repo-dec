package com.upi.unit.service;

import com.upi.dto.TransferRequest;
import com.upi.dto.TransferResponse;
import com.upi.entity.Transaction;
import com.upi.entity.Transaction.TransactionStatus;
import com.upi.exception.PaymentException;
import com.upi.repository.TransactionRepository;
import com.upi.service.ChargeCalculatorService;
import com.upi.service.TransferService;
import com.upi.service.VpaValidatorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TransferService
 * Tests service layer in isolation with mocked dependencies
 * 
 * TEST PYRAMID: UNIT TESTS (70%)
 * - Fast execution
 * - No Spring context
 * - All dependencies mocked
 */
@DisplayName("TransferService - Unit Tests")
class TransferServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private VpaValidatorService vpaValidatorService;

    @Mock
    private ChargeCalculatorService chargeCalculatorService;

    private TransferService transferService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        transferService = new TransferService(transactionRepository, vpaValidatorService, chargeCalculatorService);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    // ========================================================================
    // TRANSFER PROCESSING - HAPPY PATH
    // ========================================================================

    @Test
    @DisplayName("✅ Valid P2P transfer should succeed")
    void validP2PTransfer_shouldSucceed() {
        // Arrange
        TransferRequest request = createValidRequest("P2P");
        setupMocksForSuccessfulTransfer();

        // Act
        TransferResponse response = transferService.processTransfer(request);

        // Assert
        assertAll("Transfer response",
            () -> assertNotNull(response),
            () -> assertEquals(TransactionStatus.SUCCESS, response.getStatus()),
            () -> assertNotNull(response.getTransactionRef()),
            () -> assertTrue(response.getTransactionRef().startsWith("TXN"))
        );

        // Verify interactions
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("✅ Valid P2M transfer should succeed")
    void validP2MTransfer_shouldSucceed() {
        // Arrange
        TransferRequest request = createValidRequest("P2M");
        setupMocksForSuccessfulTransfer();

        // Act
        TransferResponse response = transferService.processTransfer(request);

        // Assert
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
    }

    @Test
    @DisplayName("✅ Transfer should include calculated charges")
    void transfer_shouldIncludeCharges() {
        // Arrange
        TransferRequest request = createValidRequest("P2M");
        setupMocksForSuccessfulTransfer();

        // Act
        TransferResponse response = transferService.processTransfer(request);

        // Assert
        assertNotNull(response.getCharges());
        verify(chargeCalculatorService).calculateCharges(any(), any(), anyBoolean());
    }

    @ParameterizedTest(name = "Transfer type: {0}")
    @DisplayName("✅ All valid transfer types should succeed")
    @ValueSource(strings = {"P2P", "P2M", "BILL"})
    void allTransferTypes_shouldSucceed(String type) {
        // Arrange
        TransferRequest request = createValidRequest(type);
        setupMocksForSuccessfulTransfer();

        // Act
        TransferResponse response = transferService.processTransfer(request);

        // Assert
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
    }

    // ========================================================================
    // TRANSFER PROCESSING - ERROR SCENARIOS
    // ========================================================================

    @Test
    @DisplayName("❌ Null request should throw exception")
    void nullRequest_shouldThrowException() {
        assertThrows(PaymentException.class, 
            () -> transferService.processTransfer(null));
    }

    @Test
    @DisplayName("❌ Invalid payer VPA should throw exception")
    void invalidPayerVPA_shouldThrowException() {
        // Arrange
        TransferRequest request = createValidRequest("P2P");
        when(vpaValidatorService.isValidFormat("invalid")).thenReturn(false);
        when(vpaValidatorService.containsBlockedPattern("invalid")).thenReturn(false);
        request.setPayerVpa("invalid");

        // Act & Assert
        PaymentException ex = assertThrows(PaymentException.class,
            () -> transferService.processTransfer(request));
        assertTrue(ex.getMessage().contains("VPA"));
    }

    @Test
    @DisplayName("❌ Same payer and payee should throw exception")
    void samePayerAndPayee_shouldThrowException() {
        // Arrange
        TransferRequest request = createValidRequest("P2P");
        request.setPayeeVpa(request.getPayerVpa());
        when(vpaValidatorService.isValidFormat(any())).thenReturn(true);
        when(vpaValidatorService.containsBlockedPattern(any())).thenReturn(false);
        when(vpaValidatorService.areDifferentVPAs(any(), any())).thenReturn(false);

        // Act & Assert
        PaymentException ex = assertThrows(PaymentException.class,
            () -> transferService.processTransfer(request));
        assertTrue(ex.getMessage().toLowerCase().contains("same"));
    }

    @ParameterizedTest(name = "Invalid amount: {0}")
    @DisplayName("❌ Invalid amounts should throw exception")
    @CsvSource({
        "0, Amount must be between",
        "-100, Amount must be between",
        "200000, Amount must be between"
    })
    void invalidAmounts_shouldThrowException(String amount, String expectedMessage) {
        // Arrange
        TransferRequest request = createValidRequest("P2P");
        request.setAmount(new BigDecimal(amount));
        when(vpaValidatorService.isValidFormat(any())).thenReturn(true);
        when(vpaValidatorService.containsBlockedPattern(any())).thenReturn(false);
        when(vpaValidatorService.areDifferentVPAs(any(), any())).thenReturn(true);

        // Act & Assert
        PaymentException ex = assertThrows(PaymentException.class,
            () -> transferService.processTransfer(request));
        assertTrue(ex.getMessage().contains(expectedMessage));
    }

    @Test
    @DisplayName("❌ Blocked VPA pattern should throw exception")
    void blockedVPAPattern_shouldThrowException() {
        // Arrange
        TransferRequest request = createValidRequest("P2P");
        request.setPayerVpa("frauduser@sbi");
        when(vpaValidatorService.isValidFormat(any())).thenReturn(true);
        when(vpaValidatorService.containsBlockedPattern("frauduser@sbi")).thenReturn(true);

        // Act & Assert
        assertThrows(PaymentException.class,
            () -> transferService.processTransfer(request));
    }

    // ========================================================================
    // TRANSACTION STATUS
    // ========================================================================

    @Test
    @DisplayName("✅ Should return status for existing transaction")
    void existingTransaction_shouldReturnStatus() {
        // Arrange
        String txnRef = "TXN12345";
        Transaction mockTxn = new Transaction();
        mockTxn.setTransactionRef(txnRef);
        mockTxn.setStatus(TransactionStatus.SUCCESS);
        mockTxn.setAmount(new BigDecimal("1000"));
        mockTxn.setCharges(BigDecimal.ZERO);
        mockTxn.setPayerVpa("user@sbi");
        mockTxn.setPayeeVpa("merchant@hdfc");

        when(transactionRepository.findByTransactionRef(txnRef))
            .thenReturn(Optional.of(mockTxn));

        // Act
        TransferResponse response = transferService.getTransactionStatus(txnRef);

        // Assert
        assertAll("Transaction status",
            () -> assertEquals(txnRef, response.getTransactionRef()),
            () -> assertEquals(TransactionStatus.SUCCESS, response.getStatus())
        );
    }

    @Test
    @DisplayName("❌ Should throw exception for non-existent transaction")
    void nonExistentTransaction_shouldThrowException() {
        // Arrange
        when(transactionRepository.findByTransactionRef("INVALID_REF"))
            .thenReturn(Optional.empty());

        // Act & Assert
        PaymentException ex = assertThrows(PaymentException.class,
            () -> transferService.getTransactionStatus("INVALID_REF"));
        assertEquals("TXN_NOT_FOUND", ex.getErrorCode());
    }

    // ========================================================================
    // VPA VALIDATION
    // ========================================================================

    @ParameterizedTest(name = "Valid VPA: {0}")
    @DisplayName("✅ Valid VPAs should pass validation")
    @ValueSource(strings = {"user@sbi", "john.doe@hdfc", "merchant@axis"})
    void validVPAs_shouldPass(String vpa) {
        // Arrange
        when(vpaValidatorService.isValidFormat(vpa)).thenReturn(true);
        when(vpaValidatorService.containsBlockedPattern(vpa)).thenReturn(false);

        // Act & Assert
        assertTrue(transferService.isValidVpa(vpa));
    }

    @ParameterizedTest(name = "Invalid VPA: {0}")
    @DisplayName("❌ Invalid VPAs should fail validation")
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "@sbi", "user@"})
    void invalidVPAs_shouldFail(String vpa) {
        // Arrange
        when(vpaValidatorService.isValidFormat(vpa)).thenReturn(false);

        // Act & Assert
        assertFalse(transferService.isValidVpa(vpa));
    }

    // ========================================================================
    // AMOUNT VALIDATION
    // ========================================================================

    @ParameterizedTest(name = "Valid amount: {0}")
    @DisplayName("✅ Valid amounts should pass")
    @ValueSource(strings = {"1", "100", "1000", "50000", "100000"})
    void validAmounts_shouldPass(String amount) {
        assertTrue(transferService.isValidAmount(new BigDecimal(amount)));
    }

    @ParameterizedTest(name = "Invalid amount: {0}")
    @DisplayName("❌ Invalid amounts should fail")
    @ValueSource(strings = {"0", "-1", "0.50", "100001", "500000"})
    void invalidAmounts_shouldFail(String amount) {
        assertFalse(transferService.isValidAmount(new BigDecimal(amount)));
    }

    @Test
    @DisplayName("❌ Null amount should fail")
    void nullAmount_shouldFail() {
        assertFalse(transferService.isValidAmount(null));
    }

    @Test
    @DisplayName("✅ Boundary: Minimum amount should pass")
    void minimumAmount_shouldPass() {
        assertTrue(transferService.isValidAmount(new BigDecimal("1.00")));
    }

    @Test
    @DisplayName("✅ Boundary: Maximum amount should pass")
    void maximumAmount_shouldPass() {
        assertTrue(transferService.isValidAmount(new BigDecimal("100000.00")));
    }

    // ========================================================================
    // TRANSACTION REFERENCE GENERATION
    // ========================================================================

    @Test
    @DisplayName("✅ Generated reference should have correct format")
    void generatedRef_shouldHaveCorrectFormat() {
        String ref = transferService.generateTransactionRef();

        assertAll("Reference format",
            () -> assertNotNull(ref),
            () -> assertTrue(ref.startsWith("TXN")),
            () -> assertEquals(19, ref.length())
        );
    }

    @Test
    @DisplayName("✅ Generated references should be unique")
    void generatedRefs_shouldBeUnique() {
        String ref1 = transferService.generateTransactionRef();
        String ref2 = transferService.generateTransactionRef();
        String ref3 = transferService.generateTransactionRef();

        assertAll("Uniqueness",
            () -> assertNotEquals(ref1, ref2),
            () -> assertNotEquals(ref2, ref3),
            () -> assertNotEquals(ref1, ref3)
        );
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private TransferRequest createValidRequest(String type) {
        TransferRequest request = new TransferRequest();
        request.setPayerVpa("payer@sbi");
        request.setPayeeVpa("payee@hdfc");
        request.setAmount(new BigDecimal("1000.00"));
        request.setTransactionType(type);
        request.setRemarks("Test transfer");
        return request;
    }

    private void setupMocksForSuccessfulTransfer() {
        // VPA validation
        when(vpaValidatorService.isValidFormat(any())).thenReturn(true);
        when(vpaValidatorService.containsBlockedPattern(any())).thenReturn(false);
        when(vpaValidatorService.areDifferentVPAs(any(), any())).thenReturn(true);
        when(vpaValidatorService.isSameBank(any(), any())).thenReturn(false);

        // Charge calculation
        ChargeCalculatorService.ChargeResult chargeResult = new ChargeCalculatorService.ChargeResult();
        chargeResult.setTotalCharges(BigDecimal.ZERO);
        chargeResult.setNetAmount(new BigDecimal("1000.00"));
        when(chargeCalculatorService.calculateCharges(any(), any(), anyBoolean()))
            .thenReturn(chargeResult);

        // Repository
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
