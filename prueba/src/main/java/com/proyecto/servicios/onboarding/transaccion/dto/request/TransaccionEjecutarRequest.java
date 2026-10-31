package com.proyecto.servicios.onboarding.transaccion.dto.request;

import com.proyecto.servicios.onboarding.transaccion.validation.MontoValido;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request de ejecucion de transaccion. Alineado con el documento de integracion:
 * identificadores por body (no @PathVariable), idempotencyKey obligatoria para prevenir
 * cargos duplicados. El cliente dueno sale del JWT (seguridad.jwt.enabled=true) o del
 * campo clienteId cuando JWT esta deshabilitado.
 */
@Getter @Setter @NoArgsConstructor
public class TransaccionEjecutarRequest {

    @NotNull(message = "El clienteId es obligatorio")
    private Long clienteId;

    @NotBlank(message = "La idempotencyKey es obligatoria")
    @Size(min = 8, max = 80, message = "La idempotencyKey debe tener entre 8 y 80 caracteres")
    private String idempotencyKey;

    @NotBlank(message = "El numeroCuenta es obligatorio")
    @Size(max = 18)
    private String numeroCuenta;

    @NotBlank(message = "El idServicio es obligatorio")
    @Size(max = 50)
    private String idServicio;

    @NotBlank(message = "El idProducto es obligatorio")
    @Size(max = 50)
    private String idProducto;

    @MontoValido
    private BigDecimal monto;

    /** Obligatorio para tipoFront=1 (recargas no canjeables). 10 digitos. */
    @Pattern(regexp = "^[0-9]{10}$|^#[0-9]{10}$|^$", message = "Telefono invalido: deben ser 10 digitos (o canjeable con #)")
    private String telefono;

    /** Obligatorio para tipoFront=2 (pagos de servicio). */
    @Size(max = 80)
    private String referencia;
}
