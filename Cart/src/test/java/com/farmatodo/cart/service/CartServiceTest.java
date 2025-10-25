package com.farmatodo.cart.service;

import com.farmatodo.cart.domain.CartItem;
import com.farmatodo.cart.dto.CartItemDTO;
import com.farmatodo.cart.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CartServiceTest {

    private CartItemRepository repo;
    private ProductClient productClient;
    private CartService service;

    @BeforeEach
    void setup() {
        repo = mock(CartItemRepository.class);
        productClient = mock(ProductClient.class);
        service = new CartService(repo, productClient);
        // forzar validateProduct=true (simula @Value)
        setField(service, "validateProduct", true);
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void addItem_valida_qty() {
        var ex = assertThrows(org.springframework.web.ErrorResponseException.class,
                () -> service.addItem(1L, 10L, 0));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void addItem_valida_producto_inexistente() {
        when(productClient.get(10L)).thenReturn(Optional.empty()); // no existe
        var ex = assertThrows(org.springframework.web.ErrorResponseException.class,
                () -> service.addItem(1L, 10L, 2));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        assertThat(ex.getBody().getDetail()).contains("Product not found");
    }

    @Test
    void addItem_inserta_nuevo_item() {
        when(productClient.get(10L)).thenReturn(Optional.of(new ProductSummary(10L, "Shampoo")));
        when(repo.findByCustomerIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        // Simula save asignando id
        when(repo.save(any(CartItem.class))).thenAnswer(inv -> {
            CartItem ci = inv.getArgument(0);
            ci.setId(100L);
            return ci;
        });

        CartItemDTO dto = service.addItem(1L, 10L, 3);

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.customerId()).isEqualTo(1L);
        assertThat(dto.productId()).isEqualTo(10L);
        assertThat(dto.qty()).isEqualTo(3);
        assertThat(dto.productName()).isEqualTo("Shampoo");

        // Verifica que guardó
        verify(repo, times(1)).save(any(CartItem.class));
    }

    @Test
    void addItem_acumula_si_existe() {
        when(productClient.get(10L)).thenReturn(Optional.of(new ProductSummary(10L, "Shampoo")));
        var existente = CartItem.builder()
                .id(200L).customerId(1L).productId(10L).qty(5)
                .updatedAt(Instant.now())
                .build();
        when(repo.findByCustomerIdAndProductId(1L, 10L)).thenReturn(Optional.of(existente));
        when(repo.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItemDTO dto = service.addItem(1L, 10L, 4);

        assertThat(dto.id()).isEqualTo(200L);
        assertThat(dto.qty()).isEqualTo(9); // 5 + 4
        verify(repo).save(argThat(ci -> ci.getQty() == 9));
    }

    @Test
    void get_devuelve_items_mapeados() {
        var i1 = CartItem.builder().id(1L).customerId(7L).productId(10L).qty(2).updatedAt(Instant.now()).build();
        var i2 = CartItem.builder().id(2L).customerId(7L).productId(11L).qty(1).updatedAt(Instant.now()).build();
        when(repo.findByCustomerId(7L)).thenReturn(List.of(i1, i2));

        var list = service.get(7L);
        assertThat(list).hasSize(2);
        assertThat(list.get(0).productId()).isEqualTo(10L);
        assertThat(list.get(1).qty()).isEqualTo(1);
    }

    @Test
    void clear_elimina_por_customer() {
        service.clear(9L);
        verify(repo).deleteByCustomerId(9L);
    }
}
