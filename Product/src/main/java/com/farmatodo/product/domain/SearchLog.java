package com.farmatodo.product.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="search_logs", indexes = @Index(name="idx_search_q", columnList = "q"))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SearchLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String q;
    @Column(nullable=false) private Instant createdAt;

    @PrePersist void onCreate(){ if (createdAt==null) createdAt = Instant.now(); }
}
