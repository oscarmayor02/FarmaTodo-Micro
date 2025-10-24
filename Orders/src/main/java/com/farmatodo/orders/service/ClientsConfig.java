package com.farmatodo.orders.service;

import org.springframework.context.annotation.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientsConfig {

    @Bean
    public WebClient webClient() {
        var http = reactor.netty.http.client.HttpClient.create()
                .responseTimeout(java.time.Duration.ofSeconds(5))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
