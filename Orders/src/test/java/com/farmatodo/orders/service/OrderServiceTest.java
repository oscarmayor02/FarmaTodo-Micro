package com.farmatodo.orders.service;

import com.farmatodo.orders.domain.Order;
import com.farmatodo.orders.domain.OrderItem;
import com.farmatodo.orders.domain.OrderStatus;
import com.farmatodo.orders.dto.CreateOrderRequest;
import com.farmatodo.orders.dto.OrderResponse;
import com.farmatodo.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.ErrorResponseException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;


class OrderServiceTest {

    private OrderRepository repo;
    private CustomerClient customers;
    private ProductClient products;
    private PaymentsClient payments;
    private AuditClient audit;

    private OrderService service;

    // Tipos que usa OrderService en tiempo de ejecución
    // (ajusta los imports si tus DTOs reales viven en otro paquete)
    static class ProductDTO {
        private final long price; // en centavos
        private final int stock;

        ProductDTO(long price, int stock) {
            this.price = price;
            this.stock = stock;
        }

        public long price() {
            return price;
        }

        public int stock() {
            return stock;
        }
    }

    interface ChargeResponseView {
        String status();

        Integer attempts();
    }

    @BeforeEach
    void setup() {
        repo = Mockito.mock(OrderRepository.class);
        customers = Mockito.mock(CustomerClient.class);
        products = Mockito.mock(ProductClient.class);
        payments = Mockito.mock(PaymentsClient.class);
        audit = Mockito.mock(AuditClient.class);

        service = new OrderService(repo, customers, products, payments, audit);
    }



    @Test
    void createAndPay__validations_missing_token_and_card() {
        Mockito.when(customers.exists(1L)).thenReturn(true);
        var req = new CreateOrderRequest(
                1L, "Dir", null, null, List.of(new CreateOrderRequest.Item(1L, 1)), "x@y.com"
        );

        var ex = assertThrows(ErrorResponseException.class, () -> service.createAndPay(req));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void createAndPay__validations_customer_not_found() {
        Mockito.when(customers.exists(1L)).thenReturn(false);
        var req = new CreateOrderRequest(
                1L, "Dir", "tok", null, List.of(new CreateOrderRequest.Item(1L, 1)), "x@y.com"
        );

        var ex = assertThrows(ErrorResponseException.class, () -> service.createAndPay(req));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void createAndPay__validations_items_required() {
        Mockito.when(customers.exists(1L)).thenReturn(true);
        var req = new CreateOrderRequest(
                1L, "Dir", "tok", null, List.of(), "x@y.com"
        );

        var ex = assertThrows(ErrorResponseException.class, () -> service.createAndPay(req));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void createAndPay__product_not_found() {
        Mockito.when(customers.exists(1L)).thenReturn(true);
        Mockito.when(products.get(99L)).thenReturn(null);

        var req = new CreateOrderRequest(
                1L, "Dir", "tok", null, List.of(new CreateOrderRequest.Item(99L, 1)), "x@y.com"
        );

        var ex = assertThrows(ErrorResponseException.class, () -> service.createAndPay(req));
        assertEquals(400, ex.getStatusCode().value());
    }


    @Test
    void get__ok() {
        Order o = Order.builder()
                .id(5L)
                .customerId(1L)
                .status(OrderStatus.PAID)
                .totalAmount(new BigDecimal("123.45"))
                .createdAt(Instant.now())
                .items(List.of(OrderItem.builder()
                        .productId(7L).qty(2).unitPrice(new BigDecimal("61.72")).subtotal(new BigDecimal("123.44")).build()))
                .build();

        Mockito.when(repo.findById(5L)).thenReturn(Optional.of(o));

        var res = service.get(5L);
        assertEquals(5L, res.orderId());
        assertEquals("PAID", res.status());
        assertEquals(new BigDecimal("123.45"), res.totalAmount());
        assertEquals(1, res.items().size());
    }

    @Test
    void get__not_found() {
        Mockito.when(repo.findById(5L)).thenReturn(Optional.empty());
        var ex = assertThrows(ErrorResponseException.class, () -> service.get(5L));
        assertEquals(404, ex.getStatusCode().value());
    }
}
