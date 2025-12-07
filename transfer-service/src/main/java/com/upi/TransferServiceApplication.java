package com.upi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;

/**
 * UPI Transfer Service - Spring Boot Application
 * 
 * REST API for processing UPI payment transfers
 * 
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * API Docs: http://localhost:8080/v3/api-docs
 * Health: http://localhost:8080/actuator/health
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "UPI Transfer Service API",
        version = "1.0.0",
        description = "REST API for UPI payment transfers - Fund transfer, validation, and status tracking",
        contact = @Contact(name = "UPI Support", email = "support@upi.org")
    )
)
public class TransferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }
}
