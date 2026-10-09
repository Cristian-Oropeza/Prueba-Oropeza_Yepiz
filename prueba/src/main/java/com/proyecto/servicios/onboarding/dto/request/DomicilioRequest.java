package com.proyecto.servicios.onboarding.dto.request;

import jakarta.validation.constraints.*;

public record DomicilioRequest(
        @NotBlank @Size(min = 2, max = 120) String calle,
        @NotBlank @Size(min = 1, max = 10)  String numeroExterior,
        @Size(max = 10)                     String numeroInterior,
        @NotBlank @Size(min = 2, max = 80)  String colonia,
        @NotBlank @Size(min = 2, max = 80)  String municipio,
        @NotBlank @Size(min = 2, max = 60)  String estado,
        @NotBlank @Pattern(regexp = "^\\d{5}$", message = "El codigo postal debe tener 5 digitos")
        String codigoPostal,
        @NotBlank @Size(min = 2, max = 60)  String pais
) {}
