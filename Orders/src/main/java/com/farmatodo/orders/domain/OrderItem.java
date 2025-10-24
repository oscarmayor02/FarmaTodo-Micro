package com.farmatodo.orders.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="order_items")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id", nullable=false)
    private Order order;

    @Column(nullable=false)
    private Long productId;

    @Column(nullable=false)
    private int qty;

    @Column(nullable=false, precision=18, scale=2)
    private BigDecimal unitPrice;

    @Column(nullable=false, precision=18, scale=2)
    private BigDecimal subtotal;
}
