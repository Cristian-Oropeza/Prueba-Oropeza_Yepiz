package com.proyecto.servicios.onboarding.dto.response;

public record DomicilioResponse(
        Long id,
        String calle,
        String numeroExterior,
        String numeroInterior,
        String colonia,
        String municipio,
        String estado,
        String codigoPostal,
        String pais
) {}
