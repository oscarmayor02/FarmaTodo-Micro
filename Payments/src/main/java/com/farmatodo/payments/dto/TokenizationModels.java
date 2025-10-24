package com.farmatodo.payments.dto;


// Clases internas para mapear request/response del micro Tokenization
public class TokenizationModels {

    // Request a /api/v1/tokenize
    public record TokenizeRequest(
            String pan,     // PAN de tarjeta
            String cvv,     // CVV
            int expMonth,   // Mes
            int expYear,    // Año
            String name     // Titular
    ) {
    }

    // Response de /api/v1/tokenize
    public record TokenizeResponse(
            String token,   // Token generado (o null si REJECTED)
            String last4,   // Últimos 4
            String brand,   // Marca
            String status   // ISSUED o REJECTED
    ) {
    }
}