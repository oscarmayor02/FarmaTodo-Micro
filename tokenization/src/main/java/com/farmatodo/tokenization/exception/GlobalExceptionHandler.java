package com.farmatodo.tokenization.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

/**
 * Maneja excepciones comunes y devuelve ApiError en JSON.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // Body inválido / JSON mal formado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON body", req, ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
    }

    // Validaciones de @Valid (campos requeridos, rangos, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, details);
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid");
    }

    // Violaciones de restricción (únicos, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Data integrity violation", req, root(ex));
    }

    // Excepciones de negocio pre-mapeadas a HTTP (si lanzas ErrorResponseException)
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        String reason = status.getReasonPhrase();
        return build(status, reason, req, root(ex));
    }

    // Cifrado u otros estados internos (IllegalStateException de encrypt, por ejemplo)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Illegal state", req, root(ex));
    }

    // Catch-all (último recurso)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", req, root(ex));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, HttpServletRequest req, String message) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), error, message, req.getRequestURI()));
    }

    private String root(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null && r.getCause() != r) r = r.getCause();
        return r.getMessage() != null ? r.getMessage() : t.toString();
    }
}
