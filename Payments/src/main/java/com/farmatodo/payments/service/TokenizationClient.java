package com.farmatodo.payments.service;

import com.farmatodo.payments.dto.CardData;
import com.farmatodo.payments.dto.TokenizationModels;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP para el micro de Tokenization.
 * - Usa baseUrl del builder (evita concatenaciones).
 * - Mapea rechazo probabilístico a 422 (Unprocessable Entity).
 * - Propaga otros 4xx/5xx con el cuerpo del error.
 */
@Component
@RequiredArgsConstructor
public class TokenizationClient {

    private final WebClient webClient;

    @Value("${tokenization.base-url}")
    private String baseUrl;

    @Value("${tokenization.api-key}")
    private String apiKey;

    /**
     * Solicita tokenización de tarjeta a /api/v1/tokenize.
     */
    public TokenizationModels.TokenizeResponse tokenize(CardData card) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("tokenization.base-url is not configured");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("tokenization.api-key is not configured");
        }

        TokenizationModels.TokenizeRequest body = new TokenizationModels.TokenizeRequest(
                card.pan(), card.cvv(), card.expMonth(), card.expYear(), card.name()
        );

        return webClient
                .mutate()
                .baseUrl(baseUrl)
                .build()
                .post()
                .uri("/api/v1/tokenize")
                .header("X-API-KEY", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                // Rechazo probabilístico del micro de tokenización → 422
                .onStatus(s -> s.value() == 422, r -> r.bodyToMono(String.class)
                        .map(msg -> new WebClientResponseException(
                                "Tokenization rejected",
                                422,
                                "Unprocessable Entity",
                                null,
                                msg.getBytes(StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8)))
                // Otros 4xx
                .onStatus(HttpStatusCode::is4xxClientError, r -> r.bodyToMono(String.class)
                        .map(msg -> new WebClientResponseException(
                                "Tokenization client error",
                                r.statusCode().value(),
                                r.statusCode().toString(),
                                null,
                                msg.getBytes(StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8)))
                // 5xx
                .onStatus(HttpStatusCode::is5xxServerError, r -> r.bodyToMono(String.class)
                        .map(msg -> new WebClientResponseException(
                                "Tokenization server error",
                                r.statusCode().value(),
                                r.statusCode().toString(),
                                null,
                                msg.getBytes(StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8)))
                .bodyToMono(TokenizationModels.TokenizeResponse.class)
                .block();
    }
}
