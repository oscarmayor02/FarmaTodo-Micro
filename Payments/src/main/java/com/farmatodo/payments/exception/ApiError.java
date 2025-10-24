package com.farmatodo.payments.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

// DTO de error consistente para todas las respuestas de error
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    public final Instant timestamp = Instant.now();
    public final int status;
    public final String error;
    public final String message;
    public final String path;

    public ApiError(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}