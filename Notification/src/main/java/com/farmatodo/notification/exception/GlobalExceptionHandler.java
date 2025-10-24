package com.farmatodo.notification.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

record ApiError(int status, String error, String message, String path, Instant timestamp) {
    @JsonInclude(JsonInclude.Include.NON_NULL) static ApiError of(HttpStatus st, String msg, String path) {
        return new ApiError(st.value(), st.getReasonPhrase(), msg, path, Instant.now());
    }
}

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> body(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String msg = ex.getMostSpecificCause()!=null? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(HttpStatus.BAD_REQUEST, msg, req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fmt).collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(HttpStatus.BAD_REQUEST, msg, req.getRequestURI()));
    }
    private String fmt(FieldError fe){ return fe.getField()+": "+(fe.getDefaultMessage()!=null?fe.getDefaultMessage():"invalid"); }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> custom(ErrorResponseException ex, HttpServletRequest req) {
        HttpStatus st = (HttpStatus) ex.getStatusCode();
        return ResponseEntity.status(st).body(ApiError.of(st, root(ex), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, root(ex), req.getRequestURI()));
    }

    private String root(Throwable t){
        Throwable r=t; while(r.getCause()!=null && r.getCause()!=r) r=r.getCause();
        return r.getMessage()!=null? r.getMessage(): t.toString();
    }
}
