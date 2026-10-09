package com.proyecto.servicios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Habilita el llenado automatico de los campos @CreatedDate y @LastModifiedDate.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
