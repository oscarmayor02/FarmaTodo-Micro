package com.farmatodo.product.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="products",
        uniqueConstraints = @UniqueConstraint(name="uk_products_name", columnNames = "name"))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String name;

    // Precio en unidades menores (centavos) para evitar flotantes
    @Column(nullable=false) private Long price;

    @Column(nullable=false) private Integer stock;


}
