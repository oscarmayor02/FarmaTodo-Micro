package com.farmatodo.auditlog.config;


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

    private static final Set<String> WHITELIST = Set.of("/ping", "/v3/api-docs", "/swagger-ui");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey == null || !apiKey.equals(configuredKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or missing X-API-KEY\",\"path\":\""
                    + request.getRequestURI() + "\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
