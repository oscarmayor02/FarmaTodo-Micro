package com.farmatodo.orders.dto;

import java.util.List;

public class PaymentModels {
    public record ChargeRequest(String orderId, long amount, String currency, String token, CardData card, String customerEmail) {}
    public record ChargeResponse(String status, int attempts, String authCode, String orderId) {}
    public record CardData(String pan, String cvv, int expMonth, int expYear, String name) {}

    // Product
    public record ProductDTO(Long id, String name, long price, int stock) {}
}
