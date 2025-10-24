package com.farmatodo.tokenization.dto;

import jakarta.validation.constraints.*;

/**
 * Payload de entrada para tokenización.
 */
public record TokenizeRequest(
        @NotBlank
        String pan,

        @NotBlank
        String cvv,

        @NotNull
        @Min(1)
        @Max(12)
        Integer expMonth,

        @NotNull
        @Min(2024)
        @Max(2100)
        Integer expYear,

        @NotBlank
        String name
) {}
