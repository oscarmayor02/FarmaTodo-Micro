package com.farmatodo.customers.service;

import com.farmatodo.customers.domain.Customer;
import com.farmatodo.customers.dto.CreateCustomerRequest;
import com.farmatodo.customers.dto.CustomerResponse;
import com.farmatodo.customers.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    private CustomerRepository repo;
    private CustomerService service;

    @BeforeEach
    void setup() {
        repo = mock(CustomerRepository.class);
        service = new CustomerService(repo);
    }

    @Test
    void create_ok_persists_and_returns() {
        CreateCustomerRequest req = new CreateCustomerRequest("Andrea", "a@x.com", "+57 300", "Calle 1");
        when(repo.existsByEmail("a@x.com")).thenReturn(false);
        when(repo.existsByPhone("+57 300")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(Instant.now());
            return c;
        });

        CustomerResponse res = service.create(req);

        assertEquals("Andrea", res.name());
        assertEquals("a@x.com", res.email());
        assertNotNull(res.id());
        verify(repo).save(any(Customer.class));
    }

    @Test
    void create_conflict_email() {
        CreateCustomerRequest req = new CreateCustomerRequest("A","dup@x.com","+57 300","Calle");
        when(repo.existsByEmail("dup@x.com")).thenReturn(true);
        var ex = assertThrows(Exception.class, () -> service.create(req));
        assertTrue(ex.getMessage().contains("409") || ex.toString().contains("CONFLICT"));
    }

    @Test
    void getById_ok() {
        Customer c = Customer.builder().id(9L).name("N").email("e@x.com").phone("+57 1").address("ad").createdAt(Instant.now()).build();
        when(repo.findById(9L)).thenReturn(Optional.of(c));
        CustomerResponse res = service.getById(9L);
        assertEquals(9L, res.id());
    }

    @Test
    void getById_notFound() {
        when(repo.findById(7L)).thenReturn(Optional.empty());
        var ex = assertThrows(Exception.class, () -> service.getById(7L));
        assertTrue(ex.toString().contains("NOT_FOUND") || ex.getMessage().contains("404"));
    }

    @Test
    void update_ok_changes_fields_and_checks_uniqueness() {
        Customer existing = Customer.builder().id(1L).name("A").email("old@x.com").phone("+1").address("a").createdAt(Instant.now()).build();
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.existsByEmail("new@x.com")).thenReturn(false);
        when(repo.existsByPhone("+2")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateCustomerRequest req = new CreateCustomerRequest("New","new@x.com","+2","addr");
        CustomerResponse res = service.update(1L, req);

        assertEquals("New", res.name());
        assertEquals("new@x.com", res.email());
        assertEquals("+2", res.phone());
        assertEquals("addr", res.address());
    }

    @Test
    void patch_ok_only_selected_fields() {
        Customer existing = Customer.builder().id(2L).name("A").email("e@x.com").phone("+1").address("a").createdAt(Instant.now()).build();
        when(repo.findById(2L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String,Object> updates = Map.of("address", "temp");
        CustomerResponse res = service.patch(2L, updates);

        assertEquals("temp", res.address());
        verify(repo).save(any(Customer.class));
    }

    @Test
    void patch_conflict_email() {
        Customer existing = Customer.builder().id(3L).name("A").email("old@x.com").phone("+1").address("a").createdAt(Instant.now()).build();
        when(repo.findById(3L)).thenReturn(Optional.of(existing));
        when(repo.existsByEmail("dup@x.com")).thenReturn(true);

        Map<String,Object> updates = Map.of("email", "dup@x.com");
        var ex = assertThrows(Exception.class, () -> service.patch(3L, updates));
        assertTrue(ex.toString().contains("CONFLICT") || ex.getMessage().contains("409"));
    }

    @Test
    void patch_bad_field() {
        Customer existing = Customer.builder().id(4L).name("A").email("e@x.com").phone("+1").address("a").createdAt(Instant.now()).build();
        when(repo.findById(4L)).thenReturn(Optional.of(existing));

        Map<String,Object> updates = Map.of("unknown", "x");
        var ex = assertThrows(Exception.class, () -> service.patch(4L, updates));
        assertTrue(ex.toString().contains("BAD_REQUEST") || ex.getMessage().contains("400"));
    }

    @Test
    void delete_ok() {
        when(repo.existsById(5L)).thenReturn(true);
        service.delete(5L);
        verify(repo).deleteById(5L);
    }

    @Test
    void delete_not_found() {
        when(repo.existsById(6L)).thenReturn(false);
        var ex = assertThrows(Exception.class, () -> service.delete(6L));
        assertTrue(ex.toString().contains("NOT_FOUND") || ex.getMessage().contains("404"));
    }
}
