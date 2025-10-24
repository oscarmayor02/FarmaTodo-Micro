package com.farmatodo.orders.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        @NotBlank String address,
        // uno de los dos debe venir:
        String tokenCard,
        CardData card,
        @Size(min=1) List<Item> items,
        String customerEmail
){
    public record Item(@NotNull Long productId, @Min(1) int qty) {}
    public record CardData(@NotBlank String pan, @NotBlank String cvv,
                           @Min(1) @Max(12) int expMonth,
                           @Min(2024) int expYear,
                           @NotBlank String name) {}
}
