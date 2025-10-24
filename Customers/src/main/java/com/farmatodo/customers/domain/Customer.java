package com.farmatodo.customers.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Cliente: nombre, email (único), phone (único), address.
 * Requisito funcional del reto (registro + unicidad).
 */
@Entity
@Table(name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_customers_email", columnNames = "email"),
                @UniqueConstraint(name="uk_customers_phone", columnNames = "phone")
        })
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Customer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private String email;// único por constraint

    @Column(nullable=false)
    private String phone;// único por constraint

    @Column(nullable=false)
    private String address;

    @Column(nullable=false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
    if (createdAt == null) createdAt = Instant.now();
      }
}
