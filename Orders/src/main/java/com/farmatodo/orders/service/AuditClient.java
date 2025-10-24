package com.farmatodo.orders.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditClient {

    private final WebClient web;

    @Value("${audit.base-url:http://localhost:8088}")
    private String baseUrl;

    @Value("${audit.api-key:dev-secret}")
    private String apiKey;

    public void log(String txId, String service, String eventType, String orderId, String entityId, String payloadJson){
        String endpoint = "/api/v1/logs";
        String url = normalize(baseUrl) + endpoint;

        try {
            // 🔒 Map que permite nulls
            Map<String, Object> body = new HashMap<>();
            body.put("service", service);
            body.put("eventType", eventType);
            body.put("orderId", orderId);
            body.put("entityId", entityId);
            body.put("payloadJson", payloadJson); 

            web.post()
                    .uri(URI.create(url))
                    .header("X-API-KEY", apiKey)
                    .header("X-TX-ID", txId == null ? "" : txId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchangeToMono((ClientResponse resp) -> {
                        HttpStatusCode sc = resp.statusCode();
                        if (sc.is2xxSuccessful()) return Mono.empty();
                        return resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(b -> {
                                    System.err.println("Audit log failed: " + sc.value() + " " + sc +
                                            " | URL=" + url + " | service=" + service +
                                            " | eventType=" + eventType + " | orderId=" + orderId +
                                            " | body=" + b);
                                    return Mono.empty();
                                });
                    })
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(ex -> {
                        System.err.println("Error enviando log a Audit: " + ex.getClass().getSimpleName() +
                                " | msg=" + (ex.getMessage() == null ? "(sin mensaje)" : ex.getMessage()) +
                                " | URL=" + url);
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception ex) {
            System.err.println("Excepción al preparar envío a Audit: " + ex.getClass().getSimpleName() +
                    " | msg=" + (ex.getMessage() == null ? "(sin mensaje)" : ex.getMessage()) +
                    " | URL=" + url);
        }
    }

    private String normalize(String base) {
        if (base == null || base.isBlank()) return baseUrl;
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
