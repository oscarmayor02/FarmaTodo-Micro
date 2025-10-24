package com.farmatodo.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentEmailRequest(
        @NotBlank String eventType,      // PAYMENT_SUCCEEDED | PAYMENT_FAILED
        @NotBlank String orderId,
        @Email   String customerEmail,
        @NotNull @Positive Long amount,  // centavos
        @NotBlank String currency,       // COP, USD...
        @NotNull Integer attempts,
        @NotBlank String status          // APPROVED | REJECTED
) {}
