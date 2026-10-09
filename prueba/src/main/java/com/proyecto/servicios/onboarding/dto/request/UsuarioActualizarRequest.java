package com.proyecto.servicios.onboarding.dto.request;

import jakarta.validation.constraints.NotNull;

/** PUT /usuarios/agregar: activa o desactiva usuario por clienteId. */
public record UsuarioActualizarRequest(
        @NotNull Long clienteId,
        @NotNull Boolean activo
) {}
