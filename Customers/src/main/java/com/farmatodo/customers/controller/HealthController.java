package com.farmatodo.customers.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** /ping requerido por el reto, para todas las APIs. */
@RestController
public class HealthController {
    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("message", "pong");
    }
}
