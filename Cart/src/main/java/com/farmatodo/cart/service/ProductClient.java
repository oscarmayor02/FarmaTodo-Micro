package com.farmatodo.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class ProductClient {
    private final WebClient web;

    @Value("${clients.product.base-url}")
    private String baseUrl;
    @Value("${clients.product.api-key}")
    private String apiKey;

    public ProductClient(WebClient web){ this.web = web; }

    public Optional<ProductSummary> get(Long productId){
        try {
            var product = web.mutate().baseUrl(baseUrl).build()
                    .get().uri("/api/v1/products/{id}", productId)
                    .header("X-API-KEY", apiKey)
                    .retrieve()
                    .bodyToMono(ProductSummary.class)
                    .block();
            return Optional.ofNullable(product);
        } catch (WebClientResponseException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            // Si el servicio de productos está caído, decide si quieres propagar error o tratar como "no existe"
            return Optional.empty();
        }
    }
}
