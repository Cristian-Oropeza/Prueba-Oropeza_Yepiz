package com.proyecto.servicios.onboarding.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto estándar de paginación. Validaciones centralizadas aquí; el
 * GlobalExceptionHandler se encarga de convertir cualquier violación en
 * respuesta JSON uniforme sin tocar los controllers.
 */
@Getter @Setter @NoArgsConstructor
public class PaginacionRequest {

    @Min(value = 0, message = "El numero de pagina debe ser mayor o igual a cero")
    private Integer page = 0;

    @Min(value = 1, message = "El tamanio de pagina debe ser mayor a cero")
    @Max(value = 100, message = "El tamanio de pagina no puede exceder 100")
    private Integer size = 20;

    /** Formato "campo,direccion"  ej: "fechaCreacion,desc"  */
    private String sort = "fechaCreacion,desc";
}
