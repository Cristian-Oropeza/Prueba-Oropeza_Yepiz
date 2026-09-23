package com.proyecto.servicios.config;

import com.proyecto.servicios.client.GestoPagoProductErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

/**
 * Configuracion exclusiva del cliente de productos.
 * No lleva @Configuration para no aplicar el token a los demas clientes Feign.
 */
public class GestoPagoProductFeignConfig {

    private static final String BEARER_PREFIX = "Bearer ";

    @Bean
    public RequestInterceptor gestoPagoProductAuthInterceptor(
            @Value("${gestopago.product.token}") String token) {
        return template -> template.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
    }

    @Bean
    public ErrorDecoder gestoPagoProductErrorDecoder() {
        return new GestoPagoProductErrorDecoder();
    }
}