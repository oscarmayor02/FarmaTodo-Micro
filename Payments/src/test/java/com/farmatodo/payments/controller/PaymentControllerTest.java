package com.farmatodo.payments.controller;

import com.farmatodo.payments.dto.ChargeRequest;
import com.farmatodo.payments.dto.ChargeResponse;
import com.farmatodo.payments.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

// Tests del controlador usando MockMvc
@WebMvcTest(controllers = PaymentController.class)
class PaymentControllerTest {

    // Inyección de MockMvc para simular HTTP
    @Autowired
    private MockMvc mvc;

    // Mock del servicio
    @MockBean
    private PaymentService service;

    // Jackson para serializar a JSON
    @Autowired
    private ObjectMapper om;

    private static final String API_KEY = "dev-secret";


    private static final String BASE = "/api/v1/payments";

    // Test POST /payments/charge 200
    @Test
    void charge_ok_200() throws Exception {
        // Preparamos request
        var req = new ChargeRequest("order-1", 1000, "COP", "tok", null,null);
        // Respuesta simulada del servicio
        var res = new ChargeResponse("APPROVED", 1, "ABC123", "order-1");

        // Mockeamos el servicio
        Mockito.when(service.charge(Mockito.any())).thenReturn(res);

        // Ejecutamos la llamada HTTP
        mvc.perform(
                        MockMvcRequestBuilders.post(BASE + "/charge")
                                .header("X-API-KEY", API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(req))
                )
                // Validamos 200
                .andExpect(MockMvcResultMatchers.status().isOk())
                // Validamos campos de la respuesta
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("APPROVED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value("order-1"));
    }

    // Test GET /payments/{id} 200
    @Test
    void getById_ok_200() throws Exception {
        var res = new ChargeResponse("APPROVED", 1, "ZZZ999", "order-9");
        Mockito.when(service.getById(9L)).thenReturn(res);

        mvc.perform(MockMvcRequestBuilders.get(BASE + "/9")
                                .header("X-API-KEY", API_KEY)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.authCode").value("ZZZ999"));
    }
}
