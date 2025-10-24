package com.farmatodo.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Payload para reemplazar (PUT) un producto completo. */
public record UpdateProductRequest(
        @NotBlank String name,
        @NotNull @Min(0) Long price,
        @NotNull @Min(0) Integer stock
) {}
