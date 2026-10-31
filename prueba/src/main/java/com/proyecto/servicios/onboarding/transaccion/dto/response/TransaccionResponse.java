package com.proyecto.servicios.onboarding.transaccion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransaccionResponse {
    private Long id;
    private String referenciaOperacion;
    private String idempotencyKey;
    private Long clienteId;
    private Long cuentaId;
    private String idServicio;
    private String idProducto;
    private BigDecimal monto;
    private String moneda;
    private EstadoTransaccion estado;
    private String upc;
    private String codigoProveedor;
    private String textoProveedor;
    private String idTxProveedor;
    private Long numAutorizacion;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaEnvioProveedor;
    private OffsetDateTime fechaConfirmacion;
}
