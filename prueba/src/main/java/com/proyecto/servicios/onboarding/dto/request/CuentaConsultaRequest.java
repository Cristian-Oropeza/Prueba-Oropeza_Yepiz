package com.proyecto.servicios.onboarding.dto.request;

import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consulta unificada de cuentas. Todos los filtros opcionales, combinables.
 */
@Getter @Setter @NoArgsConstructor
public class CuentaConsultaRequest {

    @Pattern(regexp = "^$|^\\d{18}$", message = "El numero de cuenta debe tener 18 digitos")
    private String numeroCuenta;

    @Positive(message = "El clienteId debe ser positivo")
    private Long clienteId;

    private EstatusCuenta estatus;

    @Valid
    private PaginacionRequest paginacion = new PaginacionRequest();
}
