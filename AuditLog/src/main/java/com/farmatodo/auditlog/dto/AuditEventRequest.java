package com.farmatodo.auditlog.dto;


import jakarta.validation.constraints.NotBlank;

public record AuditEventRequest(
        @NotBlank String service,
        @NotBlank String eventType,
        String orderId,
        String entityId,
        @NotBlank String payloadJson
) {}