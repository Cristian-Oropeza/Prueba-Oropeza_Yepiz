package com.proyecto.servicios.onboarding.dto.request;

import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;

/** Solo el estatus es modificable. numero_cuenta y saldo NO. */
public record CuentaUpdateRequest(
        EstatusCuenta estatus
) {}
