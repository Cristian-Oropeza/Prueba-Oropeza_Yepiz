package com.proyecto.servicios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApi {

    @Bean
    public OpenAPI openAPI(@Value("${openapi.url.dev}") String serverUrl,
                          @Value("${openapi.description.dev}") String serverDescription) {
        return new OpenAPI()
                .info(new Info().title("API Servicios").version("1.0"))
                .addServersItem(new Server().url(serverUrl).description(serverDescription));
    }
}
