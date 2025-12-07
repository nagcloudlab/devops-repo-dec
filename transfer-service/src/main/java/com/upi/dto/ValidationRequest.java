package com.upi.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * VPA Validation Request DTO
 */
@Schema(description = "VPA Validation Request")
public class ValidationRequest {

    @NotBlank(message = "VPA is required")
    @Schema(description = "Virtual Payment Address to validate", example = "user@sbi", required = true)
    private String vpa;

    public ValidationRequest() {}

    public ValidationRequest(String vpa) {
        this.vpa = vpa;
    }

    public String getVpa() { return vpa; }
    public void setVpa(String vpa) { this.vpa = vpa; }
}
