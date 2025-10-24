package com.farmatodo.customers.service;

import com.farmatodo.customers.domain.Customer;
import com.farmatodo.customers.dto.CreateCustomerRequest;
import com.farmatodo.customers.dto.CustomerResponse;
import com.farmatodo.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Lógica de negocio completa para Customers (CRUD).
 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repo;

    /** -------------------------- CREATE -------------------------- **/
    public CustomerResponse create(CreateCustomerRequest req) {
        if (repo.existsByEmail(req.email())) throw conflict("Email already registered");
        if (repo.existsByPhone(req.phone())) throw conflict("Phone already registered");

        Customer saved = repo.save(Customer.builder()
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .address(req.address())
                .createdAt(Instant.now())
                .build());

        return map(saved);
    }

    /** -------------------------- READ -------------------------- **/
    public List<CustomerResponse> getAll() {
        return repo.findAll().stream().map(this::map).toList();
    }

    public CustomerResponse getById(Long id) {
        Customer c = repo.findById(id).orElseThrow(() -> notFound("Customer not found"));
        return map(c);
    }

    /** -------------------------- UPDATE (PUT) -------------------------- **/
    public CustomerResponse update(Long id, CreateCustomerRequest req) {
        Customer existing = repo.findById(id).orElseThrow(() -> notFound("Customer not found"));

        // Validar duplicados (permitiendo que mantenga su mismo email/phone)
        if (!existing.getEmail().equals(req.email()) && repo.existsByEmail(req.email()))
            throw conflict("Email already registered");
        if (!existing.getPhone().equals(req.phone()) && repo.existsByPhone(req.phone()))
            throw conflict("Phone already registered");

        existing.setName(req.name());
        existing.setEmail(req.email());
        existing.setPhone(req.phone());
        existing.setAddress(req.address());

        return map(repo.save(existing));
    }

    /** -------------------------- PATCH -------------------------- **/
    public CustomerResponse patch(Long id, Map<String, Object> updates) {
        Customer existing = repo.findById(id).orElseThrow(() -> notFound("Customer not found"));

        // Validar y aplicar solo campos presentes
        updates.forEach((k, v) -> {
            switch (k) {
                case "name" -> existing.setName((String) v);
                case "email" -> {
                    String newEmail = (String) v;
                    if (!existing.getEmail().equals(newEmail) && repo.existsByEmail(newEmail))
                        throw conflict("Email already registered");
                    existing.setEmail(newEmail);
                }
                case "phone" -> {
                    String newPhone = (String) v;
                    if (!existing.getPhone().equals(newPhone) && repo.existsByPhone(newPhone))
                        throw conflict("Phone already registered");
                    existing.setPhone(newPhone);
                }
                case "address" -> existing.setAddress((String) v);
                default -> throw badRequest("Unknown field: " + k);
            }
        });

        return map(repo.save(existing));
    }

    /** -------------------------- DELETE -------------------------- **/
    public void delete(Long id) {
        if (!repo.existsById(id)) throw notFound("Customer not found");
        repo.deleteById(id);
    }

    /** -------------------------- HELPERS -------------------------- **/
    private CustomerResponse map(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(),
                c.getAddress(), c.getCreatedAt());
    }

    private ErrorResponseException conflict(String m) {
        ErrorResponseException ex = new ErrorResponseException(HttpStatus.CONFLICT);
        ex.setDetail(m);
        return ex;
    }

    private ErrorResponseException notFound(String m) {
        ErrorResponseException ex = new ErrorResponseException(HttpStatus.NOT_FOUND);
        ex.setDetail(m);
        return ex;
    }

    private ErrorResponseException badRequest(String m) {
        ErrorResponseException ex = new ErrorResponseException(HttpStatus.BAD_REQUEST);
        ex.setDetail(m);
        return ex;
    }
}
