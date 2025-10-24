package com.farmatodo.cart.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.stream.Collectors;

record ApiError(Instant timestamp, int status, String error, String message, String path){}

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> notReadable(HttpMessageNotReadableException ex, HttpServletRequest req){
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
    public ResponseEntity<ApiError> business(ErrorResponseException ex, HttpServletRequest req){
        var st = (HttpStatus) ex.getStatusCode();
        return build(st, st.getReasonPhrase(), req, ex.getDetailMessageCode()!=null? ex.getDetailMessageCode(): ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req){
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", req, root(ex));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, HttpServletRequest req, String message){
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), error, message, req.getRequestURI()));
    }
    private String root(Throwable t){ var r=t; while(r.getCause()!=null && r.getCause()!=r) r=r.getCause(); return r.getMessage()!=null? r.getMessage(): t.toString(); }
}
