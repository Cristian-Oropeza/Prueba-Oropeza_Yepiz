package com.proyecto.servicios.onboarding.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Respuesta uniforme para errores de toda la API.
 *   codigo   = HTTP status (400, 404, 409, 401, 500...)
 *   mensaje  = texto legible, nunca generico tipo "Error"
 *   detalles = lista opcional de errores por campo "campo: motivo"
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GenericResponse {
    private Integer codigo;
    private String  mensaje;
    private List<String> detalles;
}
