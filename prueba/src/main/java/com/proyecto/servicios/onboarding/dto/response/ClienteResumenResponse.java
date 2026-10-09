package com.proyecto.servicios.onboarding.dto.response;

import java.time.OffsetDateTime;

public record ClienteResumenResponse(
        Long id,
        String nombreCompleto,
        String curp,
        String rfc,
        String correo,
        Boolean activo,
        OffsetDateTime fechaCreacion
) {}
