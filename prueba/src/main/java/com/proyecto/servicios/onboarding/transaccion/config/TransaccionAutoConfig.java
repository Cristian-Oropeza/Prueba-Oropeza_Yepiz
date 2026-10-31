package com.proyecto.servicios.onboarding.transaccion.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita el binding de GestopagoProperties y @Scheduled (necesario para el ConciliacionScheduler).
 */
@Configuration
@EnableConfigurationProperties(GestopagoProperties.class)
@EnableScheduling
public class TransaccionAutoConfig {
}
