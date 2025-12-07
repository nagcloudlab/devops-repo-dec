package com.upi.exception;

import com.upi.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

/**
 * Global Exception Handler - Handles all API exceptions
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle PaymentException
     */
   @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiError> handlePaymentException(
            PaymentException ex, HttpServletRequest request) {
        
        log.error("Payment error: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        // Return 404 for "not found" errors
        HttpStatus status = "TXN_NOT_FOUND".equals(ex.getErrorCode()) 
                ? HttpStatus.NOT_FOUND 
                : HttpStatus.BAD_REQUEST;
        
        ApiError error = new ApiError(
                status.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Invalid request parameters",
                request.getRequestURI()
        );
        
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.addFieldError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        log.error("Illegal argument: {}", ex.getMessage());
        
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENT",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error: ", ex);
        
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

        /**
     * Handle invalid JSON / empty body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        log.error("Message not readable: {}", ex.getMessage());
        
        String message = "Invalid request body";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Required request body is missing")) {
                message = "Request body is required";
            } else if (ex.getMessage().contains("JSON parse error")) {
                message = "Invalid JSON format";
            }
        }
        
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST_BODY",
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle unsupported media type (wrong Content-Type)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        
        log.error("Unsupported media type: {}", ex.getContentType());
        
        ApiError error = new ApiError(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "UNSUPPORTED_MEDIA_TYPE",
                "Content-Type '" + ex.getContentType() + "' is not supported. Use 'application/json'",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }
}
