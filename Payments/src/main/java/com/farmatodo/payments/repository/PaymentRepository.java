package com.farmatodo.payments.repository;


import com.farmatodo.payments.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Repositorio Spring Data JPA para la entidad Payment
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Búsqueda conveniente por orderId
    Optional<Payment> findByOrderId(String orderId);
}