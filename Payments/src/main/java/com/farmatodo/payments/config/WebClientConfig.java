package com.farmatodo.payments.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

// Configuración de WebClient para consumir el micro Tokenization
@Configuration
public class WebClientConfig {

    // Definimos un bean WebClient con timeouts y base config
    @Bean
    public WebClient webClient(
            ObjectProvider<ClientHttpConnector> connectorProvider
    ) {
        // Creamos un builder de WebClient
       WebClient.Builder builder =
                WebClient.builder();

        // Obtenemos, si existe, un conector personalizado (no requerido para esta demo)
      ClientHttpConnector connector = connectorProvider.getIfAvailable();
        // Si existe, lo asociamos al builder
        if (connector != null) {
            builder.clientConnector(connector);
        }

        // Devolvemos el cliente construido
        return builder.build();
    }
}