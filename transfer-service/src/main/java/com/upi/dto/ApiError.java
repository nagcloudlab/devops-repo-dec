package com.upi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * API Error Response DTO
 */
@Schema(description = "API Error Response")
public class ApiError {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Error message", example = "Invalid request parameters")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/transfer")
    private String path;

    @Schema(description = "Error timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Detailed validation errors")
    private List<FieldError> fieldErrors = new ArrayList<>();

    // Constructors
    public ApiError() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(int status, String error, String message, String path) {
        this(status, error, message);
        this.path = path;
    }

    // Getters and Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public List<FieldError> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }
    public void addFieldError(String field, String message) {
        this.fieldErrors.add(new FieldError(field, message));
    }

    // Inner class for field-level errors
    @Schema(description = "Field-level validation error")
    public static class FieldError {
        @Schema(description = "Field name", example = "amount")
        private String field;

        @Schema(description = "Error message", example = "Minimum transfer amount is ₹1")
        private String message;

        public FieldError() {}

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
