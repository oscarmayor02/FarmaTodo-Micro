package com.farmatodo.orders.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CustomerClient {
    private final WebClient web;
    @Value("${clients.customer.base-url}") private String baseUrl;
    @Value("${clients.customer.api-key}") private String apiKey;

    public CustomerClient(WebClient web){ this.web = web; }

    public boolean exists(Long customerId){
        try {
            web.mutate().baseUrl(baseUrl).build()
                    .get().uri("/api/v1/customers/{id}", customerId)
                    .header("X-API-KEY", apiKey)
                    .retrieve().toBodilessEntity().block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
