package com.farmatodo.payments.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// DTO con datos de tarjeta (opcional si ya hay token)
// Solo se usa para solicitar token al micro Tokenization
public record CardData(
        // PAN: número de tarjeta (validación básica)
        @NotBlank
        String pan,
        // CVV: código de seguridad
        @NotBlank
        String cvv,
        // Mes de expiración
        @Min(1)
        @Max(12)
        int expMonth,
        // Año de expiración (>= año actual para validación simple)
        @Min(2024)
        int expYear,
        // Nombre del titular
        @NotBlank
        String name
) {}