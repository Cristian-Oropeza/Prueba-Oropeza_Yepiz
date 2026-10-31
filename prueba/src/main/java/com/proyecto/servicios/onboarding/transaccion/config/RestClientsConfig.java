package com.proyecto.servicios.onboarding.transaccion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient dedicados para auth y tx. Timeouts tomados de GestopagoProperties.
 * Separar auth/tx permite tener timeouts distintos (auth deberia ser rapido;
 * tx tolera > 62s porque GestoPago puede tardar hasta eso).
 */
@Configuration
@RequiredArgsConstructor
public class RestClientsConfig {

    private final GestopagoProperties props;

    @Bean("gestopagoAuthRestClient")
    public RestClient gestopagoAuthRestClient() {
        ClientHttpRequestFactorySettings s = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(props.getTx().getConnectTimeout())
                .withReadTimeout(java.time.Duration.ofSeconds(15)); // auth deberia responder rapido
        ClientHttpRequestFactory f = ClientHttpRequestFactories.get(s);
        return RestClient.builder().baseUrl(props.getAuth().getUrl()).requestFactory(f).build();
    }

    @Bean("gestopagoTxRestClient")
    public RestClient gestopagoTxRestClient() {
        ClientHttpRequestFactorySettings s = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(props.getTx().getConnectTimeout())
                .withReadTimeout(props.getTx().getReadTimeout());
        ClientHttpRequestFactory f = ClientHttpRequestFactories.get(s);
        return RestClient.builder().baseUrl(props.getTx().getUrl()).requestFactory(f).build();
    }
}
