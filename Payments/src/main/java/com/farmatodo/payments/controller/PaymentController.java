package com.farmatodo.payments.controller;


import com.farmatodo.payments.dto.ChargeRequest;
import com.farmatodo.payments.dto.ChargeResponse;
import com.farmatodo.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controlador REST para exponer el cobro y consulta
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    // Servicio de negocio inyectado
    private final PaymentService service;

    // Endpoint principal de cobro: POST /payments/charge
    @PostMapping("/payments/charge")
    public ResponseEntity<ChargeResponse> charge(
            @Valid @RequestBody ChargeRequest req
    ) {
        var res = service.charge(req);
        return ResponseEntity.ok(res);
    }

    // Consulta por id de pago: GET /payments/{id}
    @GetMapping("/payments/{id}")
    public ResponseEntity<ChargeResponse> getById(
            @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(service.getById(id));
    }
}
