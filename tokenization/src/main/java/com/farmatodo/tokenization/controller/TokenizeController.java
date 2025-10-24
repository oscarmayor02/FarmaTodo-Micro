package com.farmatodo.tokenization.controller;

import com.farmatodo.tokenization.dto.TokenizeRequest;
import com.farmatodo.tokenization.dto.TokenizeResponse;
import com.farmatodo.tokenization.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * /api/v1/tokenize.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TokenizeController {

    private final TokenService service;



    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(
            @Valid @RequestBody TokenizeRequest req) {
        TokenizeResponse res = service.tokenize(req);
        if ("REJECTED".equals(res.status())) {
            return ResponseEntity.unprocessableEntity().body(res);
        }
        return ResponseEntity.ok(res);
    }
}
