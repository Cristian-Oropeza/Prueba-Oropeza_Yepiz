package com.proyecto.servicios.onboarding.transaccion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Bean de Clock inyectable. Facilita tests fijando el tiempo y evita System.currentTimeMillis()
 * disperso por la codebase.
 */
@Configuration
public class ClockConfig {
    @Bean
    public Clock clock() { return Clock.systemDefaultZone(); }
}
