package com.upi.controller;

import com.upi.dto.*;
import com.upi.entity.Transaction;
import com.upi.service.TransferService;
import com.upi.service.VpaValidatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Transfer Controller - REST API for UPI transfers
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transfer API", description = "UPI Fund Transfer Operations")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;
    private final VpaValidatorService vpaValidatorService;

    public TransferController(TransferService transferService, 
                              VpaValidatorService vpaValidatorService) {
        this.transferService = transferService;
        this.vpaValidatorService = vpaValidatorService;
    }

    // ========================================================================
    // TRANSFER OPERATIONS
    // ========================================================================

    @PostMapping("/transfer")
    @Operation(summary = "Process fund transfer", description = "Process a UPI fund transfer from payer to payee")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transfer processed successfully",
                content = @Content(schema = @Schema(implementation = TransferResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TransferResponse> processTransfer(
            @Valid @RequestBody TransferRequest request) {
        
        log.info("Received transfer request: {} -> {}", 
                request.getPayerVpa(), request.getPayeeVpa());
        
        TransferResponse response = transferService.processTransfer(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/transfer/{transactionRef}")
    @Operation(summary = "Get transaction status", description = "Get status of a transaction by reference number")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction found",
                content = @Content(schema = @Schema(implementation = TransferResponse.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TransferResponse> getTransactionStatus(
            @Parameter(description = "Transaction reference number", example = "TXN20241207120000AB")
            @PathVariable String transactionRef) {
        
        log.info("Getting status for transaction: {}", transactionRef);
        
        TransferResponse response = transferService.getTransactionStatus(transactionRef);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transfer/history/{vpa}")
    @Operation(summary = "Get transaction history", description = "Get recent transaction history for a VPA")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid VPA")
    })
    public ResponseEntity<List<Transaction>> getTransactionHistory(
            @Parameter(description = "Virtual Payment Address", example = "user@sbi")
            @PathVariable String vpa) {
        
        log.info("Getting transaction history for: {}", vpa);
        
        List<Transaction> history = transferService.getTransactionHistory(vpa);
        
        return ResponseEntity.ok(history);
    }

    // ========================================================================
    // VALIDATION OPERATIONS
    // ========================================================================

    @PostMapping("/validate/vpa")
    @Operation(summary = "Validate VPA", description = "Validate a Virtual Payment Address")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Validation completed",
                content = @Content(schema = @Schema(implementation = ValidationResponse.class)))
    })
    public ResponseEntity<ValidationResponse> validateVpa(
            @Valid @RequestBody ValidationRequest request) {
        
        log.info("Validating VPA: {}", request.getVpa());
        
        ValidationResponse response = vpaValidatorService.validate(request.getVpa());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate/vpa/{vpa}")
    @Operation(summary = "Quick VPA validation", description = "Quick check if VPA format is valid")
    public ResponseEntity<ValidationResponse> validateVpaGet(
            @Parameter(description = "VPA to validate", example = "user@sbi")
            @PathVariable String vpa) {
        
        log.info("Quick validating VPA: {}", vpa);
        
        ValidationResponse response = vpaValidatorService.validate(vpa);
        
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // HEALTH & INFO
    // ========================================================================

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is healthy")
    public ResponseEntity<HealthResponse> healthCheck() {
        return ResponseEntity.ok(new HealthResponse("UP", "Transfer Service is running"));
    }

    // Inner class for health response
    public static class HealthResponse {
        private String status;
        private String message;

        public HealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
