package com.farmatodo.orders.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.*;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> badJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON body", req, detail(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fmt).collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, details);
    }
    private String fmt(FieldError fe){ return fe.getField()+": "+(fe.getDefaultMessage()!=null?fe.getDefaultMessage():"invalid"); }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ApiError> gateway(WebClientRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, "Bad Gateway", req, ex.getMessage());
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> upstream(WebClientResponseException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
        if (status==null) status = HttpStatus.BAD_GATEWAY;
        return build(status, status.getReasonPhrase(), req, ex.getResponseBodyAsString());
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> custom(ErrorResponseException ex, HttpServletRequest req) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        return build(status, status.getReasonPhrase(), req, root(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", req, root(ex));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, HttpServletRequest req, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), error, message, req.getRequestURI()));
    }
    private String detail(HttpMessageNotReadableException ex) {
        return ex.getMostSpecificCause()!=null? ex.getMostSpecificCause().getMessage(): ex.getMessage();
    }
    private String root(Throwable t){ Throwable r=t; while(r.getCause()!=null&&r.getCause()!=r) r=r.getCause(); return r.getMessage()!=null?r.getMessage():t.toString(); }
}
