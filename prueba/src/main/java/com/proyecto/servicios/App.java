package com.proyecto.servicios;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Arranque de la aplicacion de onboarding de clientes personas fisicas.
 *
 * Escanea unicamente el modulo com.proyecto.servicios.onboarding:
 *   - Web / Validacion / JPA / Security / OpenAPI.
 */
@SpringBootApplication(scanBasePackages = "com.proyecto.servicios.onboarding")
@Slf4j
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
