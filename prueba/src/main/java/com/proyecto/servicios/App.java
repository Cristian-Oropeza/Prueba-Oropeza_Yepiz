package com.proyecto.servicios;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;

/**
 * Arranque de la aplicacion.
 *
 * Por default se escanea SOLO el modulo `onboarding`:
 *   - Nada de Feign, Mongo ni Scheduling.
 *
 * Si `gestopago.enabled=true`, la clase GestoPagoLegacyConfig se activa y
 * habilita @ComponentScan del codigo legacy + Feign + Scheduling + Mongo.
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.proyecto.servicios.onboarding",
                "com.proyecto.servicios.legacy"       // switch condicional
        },
        exclude = {
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class,
                MongoRepositoriesAutoConfiguration.class
        }
)
@Slf4j
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
