package com.farmatodo.orders.controller;

import com.farmatodo.orders.dto.CreateOrderRequest;
import com.farmatodo.orders.dto.OrderResponse;
import com.farmatodo.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        var res = service.createAndPay(req);
        return ResponseEntity.created(java.net.URI.create("/api/v1/orders/" + res.orderId())).body(res);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }
}
