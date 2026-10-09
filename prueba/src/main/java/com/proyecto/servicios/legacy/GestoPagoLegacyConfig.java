package com.proyecto.servicios.legacy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activa el modulo GestoPago (Feign + Mongo + Scheduling + escaneo del codigo legacy)
 * solo cuando `gestopago.enabled=true`.
 *
 * NOTA: si algun dia se activa este flag, tambien hay que asegurarse de que
 * MongoAutoConfiguration / MongoDataAutoConfiguration YA NO estan excluidas en App.java.
 * Como el default es apagado, aqui las declaramos por si acaso las auto-config no bastan.
 */
@Configuration
@ConditionalOnProperty(name = "gestopago.enabled", havingValue = "true")
@EnableFeignClients(basePackages = "com.proyecto.servicios.client")
@EnableScheduling
@EnableMongoRepositories(basePackages = {
        "com.proyecto.servicios.repositorys.gestopago",
        "com.proyecto.servicios.repositorys.mongo",
        "com.proyecto.servicios.repositorys.sf"
})
@ComponentScan(basePackages = {
        "com.proyecto.servicios.client",
        "com.proyecto.servicios.config",
        "com.proyecto.servicios.controller",
        "com.proyecto.servicios.document",
        "com.proyecto.servicios.exception",   // handler legacy de GestoPago
        "com.proyecto.servicios.integration",
        "com.proyecto.servicios.mapper",
        "com.proyecto.servicios.scheduler",
        "com.proyecto.servicios.service"
})
public class GestoPagoLegacyConfig {
}
