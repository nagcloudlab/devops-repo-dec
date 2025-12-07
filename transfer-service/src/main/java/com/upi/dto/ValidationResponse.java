package com.upi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * VPA Validation Response DTO
 */
@Schema(description = "VPA Validation Response")
public class ValidationResponse {

    @Schema(description = "The VPA that was validated", example = "user@sbi")
    private String vpa;

    @Schema(description = "Whether the VPA is valid", example = "true")
    private boolean valid;

    @Schema(description = "Extracted username from VPA", example = "user")
    private String username;

    @Schema(description = "Extracted bank handle from VPA", example = "sbi")
    private String bankHandle;

    @Schema(description = "Validation errors if any")
    private List<String> errors = new ArrayList<>();

    @Schema(description = "Validation warnings if any")
    private List<String> warnings = new ArrayList<>();

    // Constructors
    public ValidationResponse() {}

    public ValidationResponse(String vpa, boolean valid) {
        this.vpa = vpa;
        this.valid = valid;
    }

    // Getters and Setters
    public String getVpa() { return vpa; }
    public void setVpa(String vpa) { this.vpa = vpa; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBankHandle() { return bankHandle; }
    public void setBankHandle(String bankHandle) { this.bankHandle = bankHandle; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public void addError(String error) { this.errors.add(error); }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    public void addWarning(String warning) { this.warnings.add(warning); }

    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
}
