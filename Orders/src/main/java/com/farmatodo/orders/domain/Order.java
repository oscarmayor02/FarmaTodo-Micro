package com.farmatodo.orders.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity @Table(name="orders")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long customerId;

    @Column(nullable=false, length=512)
    private String addressSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private OrderStatus status;

    @Column(nullable=false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable=false)
    private Instant createdAt;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
    private List<OrderItem> items;

    @PrePersist void onCreate(){ if (createdAt==null) createdAt = Instant.now(); }
}
