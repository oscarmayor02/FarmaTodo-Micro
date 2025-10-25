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
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String last4;

    private String brand;// Marca (VISA/MASTERCARD/AMEX/UNKNOWN)

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)// NO NULL
    @Enumerated(EnumType.STRING)  private TokenStatus status;// Estado (ISSUED/REJECTED)

    @Column(nullable = false, columnDefinition = "bytea")
   @JdbcTypeCode(SqlTypes.BINARY)
   private byte[] encryptedPayload; // PAN|EXP|NAME cifrados

    private String payloadIvHex;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
}
