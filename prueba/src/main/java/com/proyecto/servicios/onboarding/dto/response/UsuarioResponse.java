package com.proyecto.servicios.onboarding.dto.response;

import java.time.OffsetDateTime;

public record UsuarioResponse(
        Long id,
        Long clienteId,
        String correo,
        Boolean activo,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion
) {}
