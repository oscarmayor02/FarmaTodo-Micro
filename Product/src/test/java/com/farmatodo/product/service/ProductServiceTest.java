package com.farmatodo.product.service;

import com.farmatodo.product.domain.Product;
import com.farmatodo.product.dto.CreateProductRequest;
import com.farmatodo.product.dto.ProductDTO;
import com.farmatodo.product.dto.UpdateProductRequest;
import com.farmatodo.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.ErrorResponseException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class ProductServiceTest {

    private ProductRepository repo;
    private ProductService service;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(ProductRepository.class);
        service = new ProductService(repo);
        // inyectar minStockVisible = 2 para validar la búsqueda
        setField(service, "minStockVisible", 2);
    }

    private static void setField(Object target, String name, Object val) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, val);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void create_ok() {
        var req = new CreateProductRequest("Shampoo", 15900L, 10);
        Mockito.when(repo.existsByNameIgnoreCase("Shampoo")).thenReturn(false);
        Mockito.when(repo.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductDTO dto = service.create(req);

        assertEquals(1L, dto.id());
        assertEquals("Shampoo", dto.name());
        assertEquals(15900L, dto.price());
        assertEquals(10, dto.stock());
    }

    @Test
    void create_conflict_duplicate_name() {
        var req = new CreateProductRequest("Shampoo", 15900L, 10);
        Mockito.when(repo.existsByNameIgnoreCase("Shampoo")).thenReturn(true);

        var ex = assertThrows(ErrorResponseException.class, () -> service.create(req));
        assertEquals(409, ex.getStatusCode().value());
        Mockito.verify(repo, Mockito.never()).save(any());
    }

    @Test
    void getById_ok() {
        var p = Product.builder().id(5L).name("Gel").price(2500L).stock(3).build();
        Mockito.when(repo.findById(5L)).thenReturn(Optional.of(p));

        var dto = service.getById(5L);

        assertEquals(5L, dto.id());
        assertEquals("Gel", dto.name());
    }

    @Test
    void getById_not_found() {
        Mockito.when(repo.findById(99L)).thenReturn(Optional.empty());
        var ex = assertThrows(ErrorResponseException.class, () -> service.getById(99L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void list_ok() {
        var p1 = Product.builder().id(1L).name("A").price(100L).stock(1).build();
        var p2 = Product.builder().id(2L).name("B").price(200L).stock(2).build();
        Mockito.when(repo.findAll()).thenReturn(List.of(p1, p2));

        var list = service.list();
        assertEquals(2, list.size());
    }

    @Test
    void update_ok() {
        var req = new UpdateProductRequest("Nuevo", 300L, 7);
        var p = Product.builder().id(10L).name("Viejo").price(100L).stock(1).build();
        Mockito.when(repo.findById(10L)).thenReturn(Optional.of(p));
        Mockito.when(repo.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.update(10L, req);

        assertEquals("Nuevo", dto.name());
        assertEquals(300L, dto.price());
        assertEquals(7, dto.stock());
    }

    @Test
    void update_not_found() {
        Mockito.when(repo.findById(10L)).thenReturn(Optional.empty());
        var ex = assertThrows(ErrorResponseException.class, () -> service.update(10L, new UpdateProductRequest("X", 1L, 1)));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void delete_ok() {
        Mockito.when(repo.existsById(3L)).thenReturn(true);
        service.delete(3L);
        Mockito.verify(repo).deleteById(3L);
    }

    @Test
    void delete_not_found() {
        Mockito.when(repo.existsById(3L)).thenReturn(false);
        var ex = assertThrows(ErrorResponseException.class, () -> service.delete(3L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void decrement_ok() {
        var p = Product.builder().id(2L).name("Prod").price(100L).stock(5).build();
        Mockito.when(repo.findById(2L)).thenReturn(Optional.of(p));
        Mockito.when(repo.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        service.decrement(2L, 3);

        assertEquals(2, p.getStock());
    }

    @Test
    void decrement_qty_must_be_positive() {
        var p = Product.builder().id(2L).name("Prod").price(100L).stock(5).build();
        Mockito.when(repo.findById(2L)).thenReturn(Optional.of(p));

        var ex = assertThrows(ErrorResponseException.class, () -> service.decrement(2L, 0));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void decrement_insufficient_stock() {
        var p = Product.builder().id(2L).name("Prod").price(100L).stock(2).build();
        Mockito.when(repo.findById(2L)).thenReturn(Optional.of(p));

        var ex = assertThrows(ErrorResponseException.class, () -> service.decrement(2L, 5));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void search_respects_min_stock_visible() {
        Mockito.when(repo.findByNameContainingIgnoreCaseAndStockGreaterThanEqual(eq("gel"), eq(2)))
                .thenReturn(List.of(
                        Product.builder().id(1L).name("GEL FIX").price(1000L).stock(2).build(),
                        Product.builder().id(2L).name("GEL POWER").price(1200L).stock(5).build()
                ));

        var res = service.search("gel");
        assertEquals(2, res.size());
        assertTrue(res.stream().allMatch(p -> p.stock() >= 2));
    }
}
