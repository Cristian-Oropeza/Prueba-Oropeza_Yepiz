package com.proyecto.servicios.onboarding.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class UsuarioConsultaRequest {

    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String correo;

    private Boolean activo;

    @Valid
    private PaginacionRequest paginacion = new PaginacionRequest();
}
