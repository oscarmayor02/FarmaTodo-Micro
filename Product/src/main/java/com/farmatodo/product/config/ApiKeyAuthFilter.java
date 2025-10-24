package com.farmatodo.product.config;

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

    @Value("${app.security.api-key}") private String configuredKey;

    private static final Set<String> WHITELIST = Set.of(
            "/ping", "/v3/api-docs", "/v3/api-docs/", "/swagger-ui", "/swagger-ui/", "/swagger-ui/index.html",
            "/actuator/health"
    );

    @Override protected boolean shouldNotFilter(HttpServletRequest req){
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        String p = req.getRequestURI();
        return WHITELIST.stream().anyMatch(p::startsWith);
    }

    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (configuredKey == null || configuredKey.isBlank()){
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "API Key not configured"); return;
        }
        String apiKey = req.getHeader("X-API-KEY");
        if (!safeEq(apiKey, configuredKey)){
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json");
            res.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or missing X-API-KEY\",\"path\":\""+req.getRequestURI()+"\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private boolean safeEq(String a, String b){
        if (a==null || b==null || a.length()!=b.length()) return false;
        int r=0; for(int i=0;i<a.length();i++) r |= a.charAt(i)^b.charAt(i); return r==0;
    }
}
