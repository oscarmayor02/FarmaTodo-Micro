package com.farmatodo.payments.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Controlador de salud requerido por el reto (/ping)
@RestController
public class HealthController {

    // Endpoint GET /ping que retorna {"message":"pong"}
    @GetMapping("/ping")
    public Map<String, String> ping() {
        // Retornamos un mapa inmutable con el mensaje
        return Map.of("message", "pong");
    }
}