package com.farmatodo.tokenization.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Endpoint público de salud requerido por el reto.
 */
@RestController
public class HealthController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("message", "pong");// Respuesta 200 { "message": "pong" }
    }
}