package com.farmatodo.payments.dto;


// Respuesta pública del cobro
public record ChargeResponse(
        // Estado (APPROVED o REJECTED)
        String status,
        // Intentos que se realizaron
        int attempts,
        // Código de autorización si aprueba (o null si rechazado)
        String authCode,
        // OrderId para correlación
        String orderId
) {}