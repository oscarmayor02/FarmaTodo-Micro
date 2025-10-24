package com.farmatodo.orders.service;

import com.farmatodo.orders.dto.CreateOrderRequest;
import com.farmatodo.orders.dto.PaymentModels.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PaymentsClient {
    private final WebClient web;
    @Value("${clients.payments.base-url}") private String baseUrl;
    @Value("${clients.payments.api-key}") private String apiKey;

    public PaymentsClient(WebClient web){ this.web = web; }

    public ChargeResponse charge(String orderId, long amount, String currency,
                                 String token, CreateOrderRequest.CardData card, String email) {
        CardData cardPayload = (card == null)? null :
                new CardData(card.pan(), card.cvv(), card.expMonth(), card.expYear(), card.name());

        var req = new ChargeRequest(orderId, amount, currency, token, cardPayload, email);

        return web.mutate().baseUrl(baseUrl).build()
                .post().uri("/api/v1/payments/charge")
                .header("X-API-KEY", apiKey)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ChargeResponse.class)
                .block();
    }
}
