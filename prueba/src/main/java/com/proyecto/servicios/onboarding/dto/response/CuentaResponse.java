package com.proyecto.servicios.onboarding.dto.response;

import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CuentaResponse(
        Long id,
        String numeroCuenta,
        Long clienteId,
        BigDecimal saldo,
        EstatusCuenta estatus,
        OffsetDateTime fechaApertura
) {}
