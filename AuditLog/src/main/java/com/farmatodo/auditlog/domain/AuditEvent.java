package com.farmatodo.auditlog.domain;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="audit_events", indexes = {
        @Index(name="idx_tx", columnList="txId"),
        @Index(name="idx_service", columnList="service"),
        @Index(name="idx_event_type", columnList="eventType"),
        @Index(name="idx_order", columnList="orderId")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=60)
    private String txId; // X-TX-ID

    @Column(nullable=false, length=60)
    private String service; // orders, payments, products, customers, tokenization

    @Column(nullable=false, length=80)
    private String eventType; //PAYMENT.APPROVED, ORDER.CREATED

    @Column(length=80)
    private String orderId;

    @Column(length=80)
    private String entityId; //productId, customerId, etc.

    @Column(nullable=false)
    private Instant createdAt;

    @Lob
    @Column(nullable=false)
    private String payloadJson; // cuerpo completo para auditoría
}