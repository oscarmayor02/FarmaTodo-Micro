package com.farmatodo.payments.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class ApiKeyAuthFilter extends org.springframework.web.filter.OncePerRequestFilter {

    // Inyectamos la API key configurada por variable de entorno
    @Value("${app.security.api-key}")
    private String configuredKey;

    // Conjunto de rutas públicas que no requieren API key
    private static final Set<String> WHITELIST = Set.of("/ping", "/v3/api-docs", "/swagger-ui");

    // Indica si no se debe filtrar la petición (true si está en whitelist)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Obtenemos el path de la solicitud
        String path = request.getRequestURI();
        // Si empieza por alguna ruta pública, no filtramos
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    // Lógica del filtro para validar X-API-KEY
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Leemos la cabecera X-API-KEY
        String apiKey = request.getHeader("X-API-KEY");
        // Validamos que exista y que coincida con la configurada
        if (apiKey == null || !apiKey.equals(configuredKey)) {
            // Si no coincide, respondemos 401 Unauthorized en JSON
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            // Construimos un cuerpo mínimo de error consistente
            String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or missing X-API-KEY\",\"path\":\""
                    + request.getRequestURI() + "\"}";
            response.getWriter().write(body);
            return; // No continuamos el filtro
        }
        // Si la API key es válida, continuamos la cadena de filtros
        filterChain.doFilter(request, response);
    }
}