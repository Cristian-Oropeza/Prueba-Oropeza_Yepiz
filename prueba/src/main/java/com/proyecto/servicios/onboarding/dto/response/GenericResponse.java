package com.proyecto.servicios.onboarding.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respuesta generica usada por el GlobalExceptionHandler y para respuestas
 * uniformes de error en toda la API.
 *
 *   codigo  = HTTP status (400, 404, 409, 401, 500...)
 *   mensaje = texto legible
 *   detalles = lista opcional de errores por campo ("campo: motivo"); se omite si esta vacia
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GenericResponse {

    private Integer codigo;
    private String  mensaje;
    private List<String> detalles;

    /** Factory corto para errores simples sin detalles por campo. */
    public static GenericResponse of(int codigo, String mensaje) {
        return GenericResponse.builder()
                .codigo(codigo)
                .mensaje(mensaje)
                .build();
    }

    /** Factory corto para errores con lista de detalles por campo. */
    public static GenericResponse of(int codigo, String mensaje, List<String> detalles) {
        return GenericResponse.builder()
                .codigo(codigo)
                .mensaje(mensaje)
                .detalles(detalles)
                .build();
    }
}
