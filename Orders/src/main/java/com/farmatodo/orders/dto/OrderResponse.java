package com.farmatodo.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String status,
        BigDecimal totalAmount,
        List<Item> items,
        Integer paymentAttempts,
        String paymentStatus,
        Instant createdAt
){
    public record Item(Long productId, int qty, BigDecimal unitPrice, BigDecimal subtotal) {}
}
