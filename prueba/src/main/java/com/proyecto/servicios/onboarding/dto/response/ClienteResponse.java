package com.proyecto.servicios.onboarding.dto.response;

import com.proyecto.servicios.onboarding.entity.enums.EstadoCivil;
import com.proyecto.servicios.onboarding.entity.enums.Sexo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ClienteResponse(
        Long id,
        String nombre,
        String segundoNombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreCompleto,
        LocalDate fechaNacimiento,
        String curp,
        String rfc,
        Sexo sexo,
        String nacionalidad,
        EstadoCivil estadoCivil,
        String correo,
        String telefonoMovil,
        String telefonoAlterno,
        String ocupacion,
        String empresa,
        BigDecimal ingresoMensual,
        Boolean activo,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion,
        DomicilioResponse domicilio,
        List<CuentaResponse> cuentas
) {}
