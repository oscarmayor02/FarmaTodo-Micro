package com.farmatodo.cart.dto;

public record CartItemDTO(Long id, Long customerId, Long productId, Integer qty, String productName) {}
