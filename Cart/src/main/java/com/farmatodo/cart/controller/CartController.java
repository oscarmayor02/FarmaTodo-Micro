package com.farmatodo.cart.controller;

import com.farmatodo.cart.dto.AddItemRequest;
import com.farmatodo.cart.dto.CartItemDTO;
import com.farmatodo.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class CartController {

    private final CartService service;

    @PostMapping("/carts/{customerId}/items")
    public ResponseEntity<CartItemDTO> add(@PathVariable Long customerId, @Valid @RequestBody AddItemRequest req){
        CartItemDTO dto = service.addItem(customerId, req.productId(), req.qty());
        return ResponseEntity
                .created(URI.create("/api/v1/carts/" + customerId))
                .body(dto);
    }

    @GetMapping("/carts/{customerId}")
    public ResponseEntity<List<CartItemDTO>> get(@PathVariable Long customerId){
        return ResponseEntity.ok(service.get(customerId));
    }

    @DeleteMapping("/carts/{customerId}")
    public ResponseEntity<Void> clear(@PathVariable Long customerId){
        service.clear(customerId);
        return ResponseEntity.noContent().build();
    }
}
