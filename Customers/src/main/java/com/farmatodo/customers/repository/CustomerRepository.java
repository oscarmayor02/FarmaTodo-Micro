package com.farmatodo.customers.repository;

import com.farmatodo.customers.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
