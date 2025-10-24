package com.farmatodo.tokenization.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
/**
 * Entidad que representa la emisión de un token.
 */
@Table(name = "card_tokens",
        indexes = {@Index(name = "idx_token", columnList = "token", unique = true)}) // Índice único por token
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;// Identificador interno

    @Column(nullable = false, unique = true) // Columna NO NULL y única
    private String token;// Token público (UUID)

    @Column(nullable = false)// NO NULL
    private String last4;// Últimos 4 dígitos de la tarjeta

    private String brand;// Marca (VISA/MASTERCARD/AMEX/UNKNOWN)

    @Column(nullable = false)// NO NULL
    private Instant createdAt;// Fecha de creación

    @Column(nullable = false)// NO NULL
    @Enumerated(EnumType.STRING)// Enum como string
    private TokenStatus status;// Estado (ISSUED/REJECTED)

    @Column(nullable = false, columnDefinition = "bytea")
   @JdbcTypeCode(SqlTypes.BINARY)
   private byte[] encryptedPayload; // PAN|EXP|NAME cifrados (bytea, no OID)

    private String payloadIvHex; // IV usado en AES-GCM (hex)

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
}
