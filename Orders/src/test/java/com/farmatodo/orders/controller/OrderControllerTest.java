package com.farmatodo.orders.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmatodo.orders.dto.CreateOrderRequest;
import com.farmatodo.orders.dto.OrderResponse;
import com.farmatodo.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean OrderService service;

    @Test
    void create_order_returns_201_with_body_and_location() throws Exception {
        var req = new CreateOrderRequest(
                1L,
                "Calle 1",
                "tok-abc",
                null,
                List.of(new CreateOrderRequest.Item(7L, 2)),
                "user@mail.com"
        );

        var items = List.of(new OrderResponse.Item(7L, 2, new BigDecimal("159.00"), new BigDecimal("318.00")));
        var resp = new OrderResponse(123L, "PAID", new BigDecimal("318.00"), items, 1, "APPROVED", Instant.now());

        Mockito.when(service.createAndPay(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/orders/123"))
                .andExpect(jsonPath("$.orderId").value(123))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void get_order_returns_200_with_body() throws Exception {
        var items = List.of(new OrderResponse.Item(1L, 1, new BigDecimal("10.00"), new BigDecimal("10.00")));
        var resp = new OrderResponse(99L, "PAID", new BigDecimal("10.00"), items, null, null, Instant.now());

        Mockito.when(service.get(99L)).thenReturn(resp);

        mvc.perform(get("/api/v1/orders/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(99))
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}
