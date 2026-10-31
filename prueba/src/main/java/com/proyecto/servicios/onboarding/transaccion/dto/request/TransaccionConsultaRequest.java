package com.proyecto.servicios.onboarding.transaccion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor
public class TransaccionConsultaRequest {

    @NotBlank(message = "La referenciaOperacion es obligatoria")
    @Size(max = 40)
    private String referenciaOperacion;
}
