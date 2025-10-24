package com.farmatodo.customers.controller;

import com.farmatodo.customers.dto.CreateCustomerRequest;
import com.farmatodo.customers.dto.CustomerResponse;
import com.farmatodo.customers.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST completo para el microservicio Customer.
 * Expone todos los verbs HTTP: GET, POST, PUT, PATCH, DELETE.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    /** -------------------------- GET -------------------------- **/

    // Listar todos los clientes
    @GetMapping("/customers")
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Obtener un cliente por ID
    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /** -------------------------- POST -------------------------- **/

    // Crear nuevo cliente
    @PostMapping("/customers")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    /** -------------------------- PUT -------------------------- **/

    // Reemplazar cliente completo (PUT)
    @PutMapping("/customers/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateCustomerRequest req
    ) {
        return ResponseEntity.ok(service.update(id, req));
    }

    /** -------------------------- PATCH -------------------------- **/

    // Actualizar parcialmente un cliente (solo los campos presentes)
    @PatchMapping("/customers/{id}")
    public ResponseEntity<CustomerResponse> patch(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        return ResponseEntity.ok(service.patch(id, updates));
    }

    /** -------------------------- DELETE -------------------------- **/

    // Eliminar cliente
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
