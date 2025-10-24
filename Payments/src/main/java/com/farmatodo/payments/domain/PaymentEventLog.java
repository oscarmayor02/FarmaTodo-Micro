package com.farmatodo.payments.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// Entidad para auditoría: qué pasó en cada pago, con txId y payload JSON
@Entity                                                  // Entidad JPA
@Table(name = "payment_event_logs",                      // Tabla
        indexes = {
                @jakarta.persistence.Index(name = "idx_event_tx", columnList = "txId"),     // Índice por txId
                @jakarta.persistence.Index(name = "idx_event_order", columnList = "orderId")// Índice por orderId
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventLog {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(nullable = false, length = 40)
    private String txId; // Transaction-Id

    @Column(nullable = false, length = 100)
    private PaymentStatus eventType;// PAYMENT.APPROVED / PAYMENT.REJECTED

    @Column(nullable = false, length = 80)
    private String orderId;

    @Column(nullable = false)
    private Instant createdAt;

    @Lob
    @Column(nullable = false)
    private String payloadJson;
}
