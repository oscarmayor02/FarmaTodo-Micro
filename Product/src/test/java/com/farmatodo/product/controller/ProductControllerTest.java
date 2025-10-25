package com.farmatodo.product.controller;

import com.farmatodo.product.dto.CreateProductRequest;
import com.farmatodo.product.dto.ProductDTO;
import com.farmatodo.product.dto.UpdateProductRequest;
import com.farmatodo.product.service.ProductService;
import com.farmatodo.product.service.SearchLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de capa web del ProductController
 */
@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    ProductService service;

    @MockBean
    SearchLogService searchLogService;

    @Test
    void create_returns_201_and_body() throws Exception {
        var req = new CreateProductRequest("Jabón", 990L, 5);
        var dto = new ProductDTO(10L, "Jabón", 990L, 5);

        Mockito.when(service.create(any())).thenReturn(dto);

        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/products/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Jabón"));
    }

    @Test
    void getById_ok() throws Exception {
        Mockito.when(service.getById(5L)).thenReturn(new ProductDTO(5L, "Gel", 1500L, 2));

        mvc.perform(get("/api/v1/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Gel"));
    }

    @Test
    void list_ok() throws Exception {
        Mockito.when(service.list()).thenReturn(List.of(
                new ProductDTO(1L, "A", 100L, 1),
                new ProductDTO(2L, "B", 200L, 2)
        ));

        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void update_ok() throws Exception {
        var req = new UpdateProductRequest("Nuevo", 200L, 4);
        Mockito.when(service.update(eq(7L), any())).thenReturn(
                new ProductDTO(7L, "Nuevo", 200L, 4)
        );

        mvc.perform(put("/api/v1/products/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Nuevo"));
    }

    @Test
    void delete_ok() throws Exception {
        mvc.perform(delete("/api/v1/products/9"))
                .andExpect(status().isNoContent());
        Mockito.verify(service).delete(9L);
    }

    @Test
    void decrement_ok() throws Exception {
        mvc.perform(post("/api/v1/products/3/decrement")
                        .param("qty", "2"))
                .andExpect(status().isNoContent());
        Mockito.verify(service).decrement(3L, 2);
    }

    @Test
    void search_ok() throws Exception {
        Mockito.when(service.search("gel"))
                .thenReturn(List.of(
                        new ProductDTO(1L, "GEL FIX", 1000L, 2),
                        new ProductDTO(2L, "GEL POWER", 1200L, 5)
                ));

        mvc.perform(get("/api/v1/products/search").param("q", "gel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        Mockito.verify(searchLogService).logAsync("gel");
    }
}
