package com.farmatodo.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmatodo.notification.dto.PaymentEmailRequest;
import com.farmatodo.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean EmailService emailService;

    @Test
    void si_no_hay_email__no_envia_y_devuelve_204() throws Exception {
        var req = new PaymentEmailRequest(
                "PAYMENT_SUCCEEDED",
                "ORD-1",
                null,               // sin correo
                1000L,
                "COP",
                1,
                "APPROVED"
        );

        mvc.perform(post("/api/v1/notify/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        Mockito.verify(emailService, Mockito.never()).sendHtml(anyString(), anyString(), anyString());
    }
}
