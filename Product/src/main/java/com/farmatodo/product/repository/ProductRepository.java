package com.farmatodo.product.repository;

import com.farmatodo.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCaseAndStockGreaterThanEqual(String q, Integer minStock);
    boolean existsByNameIgnoreCase(String name);

}
