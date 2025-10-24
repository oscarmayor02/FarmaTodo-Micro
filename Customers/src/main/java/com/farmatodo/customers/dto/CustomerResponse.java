package com.farmatodo.customers.dto;

import java.time.Instant;

/** Respuesta: DTO público (ocultamos internals). */
public record CustomerResponse(
        Long id, String name, String email, String phone, String address, Instant createdAt
) {}
