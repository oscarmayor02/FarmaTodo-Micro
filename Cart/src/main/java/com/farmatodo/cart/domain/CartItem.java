package com.farmatodo.cart.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="cart_items",
        uniqueConstraints=@UniqueConstraint(name="uk_cart_customer_product", columnNames={"customerId","productId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long customerId;

    @Column(nullable=false)
    private Long productId;

    @Column(nullable=false)
    private Integer qty;

    @Column(nullable=false)
    private Instant updatedAt;
}
