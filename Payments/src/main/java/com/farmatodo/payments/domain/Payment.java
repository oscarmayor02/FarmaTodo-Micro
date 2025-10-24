package com.farmatodo.payments.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// Entidad JPA para persistir los pagos
@Entity
@Table(name = "payments",
        indexes = {
                // Índice por orderId para búsquedas rápidas
                @jakarta.persistence.Index(name = "idx_payments_order", columnList = "orderId")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    // Identificador autogenerado
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    // Identificador de la orden (string para simplicidad, puede ser UUID)
    @Column(nullable = false)
    private String orderId;

    // Monto a cobrar en unidades menores (ej: centavos)
    @Column(nullable = false)
    private Long amount;

    // Moneda (ej: COP, USD)
    @Column(nullable = false, length = 10)
    private String currency;

    // Últimos 4 dígitos de la tarjeta (para auditoría)
    @Column(length = 4)
    private String last4;

    // Marca de la tarjeta (VISA/MASTERCARD/AMEX/UNKNOWN)
    @Column(length = 20)
    private String brand;

    // Token utilizado (si aplica), nunca guardamos PAN/CVV
    @Column(length = 80)
    private String token;

    // Código de autorización simulado cuando aprueba
    @Column(length = 20)
    private String authCode;

    // Número de intentos realizados (1 + reintentos)
    @Column(nullable = false)
    private Integer attempts;

    // Estado final del pago
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    // Fecha de creación del registro
    @Column(nullable = false)
    private Instant createdAt;
}
