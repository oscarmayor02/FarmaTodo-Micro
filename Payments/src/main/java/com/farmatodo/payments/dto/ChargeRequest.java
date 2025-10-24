package com.farmatodo.payments.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// Request del endpoint /payments/charge
public record ChargeRequest(
        // Identificador de la orden que se intenta cobrar
        @NotBlank
        String orderId,
        // Monto en unidades menores (long para evitar decimales)
        @Min(1)
        long amount,
        // Moneda (ej: COP)
        @NotBlank
        String currency,
        // Token de tarjeta (opcional si se envía card)
        String token,
        // Datos de tarjeta (opcional si se envía token)
        CardData card,
        String customerEmail// Email opcional para notificación

) {}