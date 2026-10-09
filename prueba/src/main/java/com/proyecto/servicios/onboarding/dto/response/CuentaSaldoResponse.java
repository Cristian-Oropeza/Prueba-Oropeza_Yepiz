package com.proyecto.servicios.onboarding.dto.response;

import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;

import java.math.BigDecimal;

public record CuentaSaldoResponse(
        String numeroCuenta,
        BigDecimal saldo,
        EstatusCuenta estatus
) {}
