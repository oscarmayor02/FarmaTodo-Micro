package com.farmatodo.customers.controller;

import com.farmatodo.customers.dto.CreateCustomerRequest;
import com.farmatodo.customers.dto.CustomerResponse;
import com.farmatodo.customers.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del controlador usando @WebMvcTest y MockMvc.
 * NOTA: si tu ApiKeyAuthFilter protege /api/v1/**,
 *       Aquí simulamos el header X-API-KEY en cada request.
 */
@WebMvcTest(controllers = CustomerController.class)
class CustomerControllerTest {

    @Autowired MockMvc mvc;
    @MockBean CustomerService service;
    @Autowired ObjectMapper om;

    private static final String API_KEY = "dev-secret";
    private static final String BASE = "/api/v1/customers";

    @Test
    void getAll_ok() throws Exception {
        var list = List.of(new CustomerResponse(1L,"A","a@x.com","+1","addr", Instant.now()));
        Mockito.when(service.getAll()).thenReturn(list);

        mvc.perform(get(BASE).header("X-API-KEY", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("a@x.com"));
    }

    @Test
    void create_201() throws Exception {
        var req = new CreateCustomerRequest("A","a@x.com","+123456789","addr");
        var res = new CustomerResponse(10L,"A","a@x.com","+123456789","addr",Instant.now());
        Mockito.when(service.create(any())).thenReturn(res);

        mvc.perform(post(BASE)
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void put_200() throws Exception {
        var req = new CreateCustomerRequest("B","b@x.com","+23456789","addr2");
        var res = new CustomerResponse(11L,"B","b@x.com","+23456789","addr2",Instant.now());
        Mockito.when(service.update(eq(11L), any())).thenReturn(res);

        mvc.perform(put(BASE + "/11")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("b@x.com"));
    }

    @Test
    void patch_200() throws Exception {
        var res = new CustomerResponse(12L,"C","c@x.com","+3456789","temp",Instant.now());
        Mockito.when(service.patch(eq(12L), any())).thenReturn(res);

        mvc.perform(patch(BASE + "/12")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("address","temp"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("temp"));
    }

    @Test
    void delete_204() throws Exception {
        mvc.perform(delete(BASE + "/13").header("X-API-KEY", API_KEY))
                .andExpect(status().isNoContent());
        Mockito.verify(service).delete(13L);
    }
}
