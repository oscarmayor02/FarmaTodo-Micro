package com.farmatodo.orders.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${app.security.api-key}")
    private String configuredKey;

    private static final Set<String> WHITELIST = Set.of(
            "/ping",
            "/v3/api-docs", "/v3/api-docs/",
            "/swagger-ui", "/swagger-ui/", "/swagger-ui/index.html",
            "/actuator/health"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (configuredKey == null || configuredKey.isBlank()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "API Key not configured");
            return;
        }
        String apiKey = request.getHeader("X-API-KEY");
        if (!constantTimeEquals(apiKey, configuredKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or missing X-API-KEY\",\"path\":\""
                    + request.getRequestURI() + "\"}");
            return;
        }
        chain.doFilter(request, response);
    }
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int r = 0; for (int i=0;i<a.length();i++) r |= a.charAt(i) ^ b.charAt(i);
        return r==0;
    }
}
