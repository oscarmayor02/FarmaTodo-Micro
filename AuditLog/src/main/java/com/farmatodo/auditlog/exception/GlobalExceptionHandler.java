package com.farmatodo.auditlog.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> notReadable(HttpMessageNotReadableException ex, HttpServletRequest req){
        return build(HttpStatus.BAD_REQUEST,"Malformed JSON body",req, detail(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req){
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField()+": "+(f.getDefaultMessage()!=null?f.getDefaultMessage():"invalid"))
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST,"Validation failed", req, details);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> errorResponse(ErrorResponseException ex, HttpServletRequest req){
        HttpStatus st = (HttpStatus) ex.getStatusCode();
        return build(st, st.getReasonPhrase(), req, root(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req){
        return build(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error", req, root(ex));
    }

    private ResponseEntity<ApiError> build(HttpStatus st, String err, HttpServletRequest req, String msg){
        return ResponseEntity.status(st).body(new ApiError(st.value(), err, msg, req.getRequestURI()));
    }
    private String detail(HttpMessageNotReadableException ex){
        return ex.getMostSpecificCause()!=null? ex.getMostSpecificCause().getMessage(): ex.getMessage();
    }
    private String root(Throwable t){
        Throwable r=t; while(r.getCause()!=null && r.getCause()!=r) r=r.getCause();
        return r.getMessage()!=null? r.getMessage(): t.toString();
    }
}