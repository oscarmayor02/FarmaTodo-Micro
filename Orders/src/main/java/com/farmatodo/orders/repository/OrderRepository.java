package com.farmatodo.orders.repository;

import com.farmatodo.orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
