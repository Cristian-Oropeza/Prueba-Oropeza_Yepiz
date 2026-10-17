package com.proyecto.servicios.onboarding.dto.request;

import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Solo el estatus es modificable. numeroCuenta y saldo son INMUTABLES.
 */
@Getter @Setter @NoArgsConstructor
public class CuentaUpdateRequest {

    @NotBlank(message = "El numero de cuenta es obligatorio")
    @Pattern(regexp = "^\\d{18}$", message = "El numero de cuenta debe tener 18 digitos")
    private String numeroCuenta;

    @NotNull(message = "El estatus es obligatorio")
    private EstatusCuenta estatus;
}
