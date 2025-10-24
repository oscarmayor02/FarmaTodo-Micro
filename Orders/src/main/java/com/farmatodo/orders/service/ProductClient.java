package com.farmatodo.orders.service;

import com.farmatodo.orders.dto.PaymentModels.ProductDTO; // si tienes un DTO específico de product, úsalo aquí
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient web;

    @Value("${clients.product.base-url}")
    private String baseUrl;

    @Value("${clients.product.api-key}")
    private String apiKey;

    public ProductDTO get(Long productId){
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/products/{id}")
                .build(productId)
                .toString();

        System.out.printf("→ [orders→product] GET %s%n", uri);

        return web.get()
                .uri(uri)
                .header("X-API-KEY", apiKey)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .block();
    }

    public void decrement(Long productId, int qty){
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/products/{id}/decrement")
                .queryParam("qty", qty)
                .build(productId)
                .toString();

        System.out.printf("→ [orders→product] POST %s%n", uri);

        web.post()
                .uri(uri)
                .header("X-API-KEY", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity()
                .block();

        System.out.printf(" [orders→product] Stock decrementado productId=%d qty=%d%n", productId, qty);
    }
}
