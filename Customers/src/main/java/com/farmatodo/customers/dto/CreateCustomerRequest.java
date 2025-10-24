package com.farmatodo.customers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Payload de creación de cliente. */
public record CreateCustomerRequest(
        @NotBlank
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message="invalid phone format")
        String phone,

        @NotBlank
        String address
) {}
