package com.farmatodo.tokenization.dto;                           // Paquete de DTOs

/**
 * Respuesta de tokenización.
 */
public record TokenizeResponse(
        String token,// Token generado (o null si REJECTED)
        String last4,// Últimos 4 dígitos para referencia
        String brand,// Marca detectada
        String status// "ISSUED" | "REJECTED"
) {}
