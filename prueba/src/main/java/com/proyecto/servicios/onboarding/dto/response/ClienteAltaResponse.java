package com.proyecto.servicios.onboarding.dto.response;

import java.time.OffsetDateTime;

public record ClienteAltaResponse(
        Long id,
        String nombreCompleto,
        String correo,
        Boolean activo,
        CuentaResponse cuenta,
        Boolean usuarioCreado,
        OffsetDateTime fechaCreacion
) {}
