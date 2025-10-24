package com.farmatodo.payments.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.stream.Collectors;

// Handler global de excepciones para devolver ApiError uniforme
@ControllerAdvice
public class GlobalExceptionHandler {

    // JSON mal formado u otros problemas al parsear el body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest req
    ) {
        // Construimos respuesta 400 con mensaje específico
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON body", req, detail(ex));
    }

    // Validaciones @Valid del request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
          HttpServletRequest req
    ) {
        // Concatenamos errores de campos
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fmt).collect(Collectors.joining("; "));
        // Devolvemos 400 con detalles
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, details);
    }

    // Formatea un error de campo a "campo: mensaje"
    private String fmt(FieldError fe) {
        return fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid");
    }

    // Excepciones de negocio con status custom (ErrorResponseException)
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> handleErrorResponse(
           ErrorResponseException ex,
           HttpServletRequest req
    ) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        return build(status, status.getReasonPhrase(), req, root(ex));
    }

    // Errores del cliente WebClient cuando Tokenization retorna 4xx/5xx/402
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> handleWebClientResponse(
           WebClientResponseException ex,
           HttpServletRequest req
    ) {
        // Devolvemos el mismo status que recibimos del micro remoto y el body de error
        HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
        if (status == null) status = HttpStatus.BAD_GATEWAY;
        return build(status, status.getReasonPhrase(), req, ex.getResponseBodyAsString());
    }

    // Último recurso: cualquier otra excepción a 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
           HttpServletRequest req
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", req, root(ex));
    }

    // Construye el ApiError con el status indicado
    private ResponseEntity<ApiError> build(
            HttpStatus status, String error, HttpServletRequest req, String message
    ) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), error, message, req.getRequestURI()));
    }

    // Extrae el detalle específico del error de lectura de cuerpo
    private String detail(HttpMessageNotReadableException ex) {
        return ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
    }

    // Extrae la causa raíz de una excepción
    private String root(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null && r.getCause() != r) r = r.getCause();
        return r.getMessage() != null ? r.getMessage() : t.toString();
    }
}