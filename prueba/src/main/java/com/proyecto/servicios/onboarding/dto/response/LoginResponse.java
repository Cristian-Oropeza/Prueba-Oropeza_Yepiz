package com.proyecto.servicios.onboarding.dto.response;

public record LoginResponse(
        String token,
        String tipo,
        Long expiraEn,
        Long clienteId,
        String correo
) {}
