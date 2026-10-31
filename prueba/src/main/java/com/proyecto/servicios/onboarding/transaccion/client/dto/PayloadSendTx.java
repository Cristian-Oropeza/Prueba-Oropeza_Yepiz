package com.proyecto.servicios.onboarding.transaccion.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.math.BigDecimal;

/**
 * Payload JSON que se cifra con AES-256-CBC antes de enviar a GestoPago. El ORDEN de los campos
 * importa segun la documentacion del proveedor: se usa JsonPropertyOrder para garantizarlo.
 * {@code JsonInclude.NON_NULL} evita enviar campos vacios que confunden al parser del proveedor.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "idProducto", "idServicio", "telefono", "horaLocal",
        "referencia", "montoPago", "upc", "unidad"
})
public class PayloadSendTx {
    private String idProducto;
    private String idServicio;
    private String telefono;
    private String horaLocal;
    private String referencia;
    private BigDecimal montoPago;
    private String upc;
    private String unidad;
}
