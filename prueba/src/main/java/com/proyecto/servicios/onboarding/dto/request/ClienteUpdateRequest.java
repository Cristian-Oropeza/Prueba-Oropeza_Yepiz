package com.proyecto.servicios.onboarding.dto.request;

import com.proyecto.servicios.onboarding.entity.enums.EstadoCivil;
import com.proyecto.servicios.onboarding.entity.enums.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * PATCH parcial: todos los campos opcionales. CURP, RFC y numero_cuenta son inmutables (se ignoran).
 */
public record ClienteUpdateRequest(

        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
        String nombre,

        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]*$")
        String segundoNombre,

        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
        String apellidoPaterno,

        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
        String apellidoMaterno,

        Sexo sexo,
        @Size(min = 2, max = 60) String nacionalidad,
        EstadoCivil estadoCivil,

        @Email @Size(max = 100) String correo,

        @Pattern(regexp = "^\\d{10}$", message = "El telefono movil debe tener 10 digitos")
        String telefonoMovil,

        @Pattern(regexp = "^\\d{10}$", message = "El telefono alterno debe tener 10 digitos")
        String telefonoAlterno,

        @Size(min = 2, max = 80)  String ocupacion,
        @Size(min = 2, max = 100) String empresa,

        @DecimalMin(value = "0.01", message = "El ingreso mensual debe ser mayor a cero")
        @Digits(integer = 10, fraction = 2)
        BigDecimal ingresoMensual,

        @Valid DomicilioRequest domicilio

) {}
