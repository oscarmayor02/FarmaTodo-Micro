package com.farmatodo.payments.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// Filtro que garantiza un Transaction-Id por request y lo coloca en MDC para trazabilidad
@Component
public class TxIdFilter extends OncePerRequestFilter {

    // Nombre del header que aceptamos/emitimos como ID de transacción
    @Value("${tracing.header:X-TX-ID}")
    private String headerName; // Header como X-TX-ID

    // Se ejecuta una vez por request HTTP
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, // Request entrante
            HttpServletResponse response,// Response saliente
            FilterChain filterChain           // Cadena de filtros
    ) throws ServletException, IOException {
        // Se lee el ID entrante; si no viene, generamos un UUID nuevo
        String txId = request.getHeader(headerName);         // Leer header
        if (txId == null || txId.isBlank()) {                // Si no vino
            txId = UUID.randomUUID().toString();   // Genero UUID
        }
        try {
            // Colocamos el txId en MDC para que aparezca en logs automáticamente
            MDC.put("txId", txId);                 // Guardamos en MDC
            // Lo devolvemos también en el response para trazabilidad cliente-servidor
            response.setHeader(headerName, txId);            // Header de salida
            // Continuamos con la cadena
            filterChain.doFilter(request, response);         // Next filter
        } finally {
            // Siempre limpiar el MDC para evitar fugas entre hilos
            MDC.remove("txId");                    // Limpieza
        }
    }
}
