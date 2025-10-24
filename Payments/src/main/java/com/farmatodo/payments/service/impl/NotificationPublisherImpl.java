package com.farmatodo.payments.service.impl;

import com.farmatodo.payments.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationPublisherImpl implements NotificationPublisher {

    private final WebClient webClient;

    @Value("${notifications.base-url}")
    private String baseUrl;

    @Value("${notifications.api-key}")
    private String apiKey;

    @Override
    public void notify(String eventType, String orderId, String customerEmail,
                       long amount, String currency, int attempts, String status) {

        Map<String, Object> body = Map.of(
                "eventType", eventType,
                "orderId", orderId,
                "customerEmail", customerEmail == null ? "" : customerEmail,
                "amount", amount,
                "currency", currency,
                "attempts", attempts,
                "status", status
        );

        try {
            System.out.printf("→ Enviando notificación %s a %s para orden %s%n", eventType, baseUrl, orderId);

            webClient.post()
                    .uri(baseUrl + "/api/v1/notify/payment")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            System.out.printf(" Notificación enviada correctamente a %s (%s)%n", customerEmail, orderId);

        } catch (Exception e) {
            System.err.printf(" Error enviando notificación: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }
}