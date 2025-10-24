package com.farmatodo.product.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> badBody(HttpMessageNotReadableException ex, HttpServletRequest req){
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON body", req,
                ex.getMostSpecificCause()!=null? ex.getMostSpecificCause().getMessage(): ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req){
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fmt).collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, details);
    }
    private String fmt(FieldError fe){ return fe.getField()+": "+(fe.getDefaultMessage()!=null?fe.getDefaultMessage():"invalid"); }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> custom(ErrorResponseException ex, HttpServletRequest req){
        HttpStatus st = (HttpStatus) ex.getStatusCode();
        return build(st, st.getReasonPhrase(), req, root(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req){
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", req, root(ex));
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ApiError> statusException(
            org.springframework.web.server.ResponseStatusException ex,
            jakarta.servlet.http.HttpServletRequest req
    ) {
        var status = org.springframework.http.HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(),
                        ex.getReason(), req.getRequestURI()));
    }

    private ResponseEntity<ApiError> build(HttpStatus st, String err, HttpServletRequest req, String msg){
        return ResponseEntity.status(st).body(new ApiError(st.value(), err, msg, req.getRequestURI()));
    }
    private String root(Throwable t){ Throwable r=t; while(r.getCause()!=null && r.getCause()!=r) r=r.getCause();
        return r.getMessage()!=null? r.getMessage(): t.toString(); }
}
