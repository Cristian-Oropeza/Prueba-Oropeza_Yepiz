package com.proyecto.servicios.onboarding.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ClienteIdRequest {

    @NotNull(message = "El id del cliente es obligatorio")
    @Positive(message = "El id debe ser positivo")
    private Long id;
}
