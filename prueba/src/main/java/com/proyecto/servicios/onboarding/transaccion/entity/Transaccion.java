package com.proyecto.servicios.onboarding.transaccion.entity;

import com.proyecto.servicios.onboarding.entity.Cliente;
import com.proyecto.servicios.onboarding.entity.Cuenta;
import com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Registro maestro de una operacion contra GestoPago. Alineado con el documento de integracion:
 *   - referencia_operacion: identificador opaco publico que se devuelve al cliente.
 *   - idempotency_key:      UNIQUE a nivel BD, previene cargos duplicados por reintentos.
 *   - upc:                  identificador local alfanumerico corto (10 chars) util para buscar en portal del proveedor.
 *   - estado:               PENDIENTE -> EN_CONFIRMACION -> {APLICADA, RECHAZADA, NO_ENVIADA}.
 */
@Entity
@Table(name = "transacciones",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_transacciones_idempotency", columnNames = "idempotency_key"),
                @UniqueConstraint(name = "uk_transacciones_referencia",  columnNames = "referencia_operacion"),
                @UniqueConstraint(name = "uk_transacciones_upc",         columnNames = "upc")
        },
        indexes = {
                @Index(name = "idx_transacciones_cliente_id",          columnList = "cliente_id"),
                @Index(name = "idx_transacciones_cuenta_estado_fecha", columnList = "cuenta_id, estado, fecha_creacion")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referencia_operacion", nullable = false, length = 40)
    private String referenciaOperacion;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transacciones_cliente"))
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transacciones_cuenta"))
    private Cuenta cuenta;

    @Column(name = "id_servicio", nullable = false, length = 50)
    private String idServicio;

    @Column(name = "id_producto", nullable = false, length = 50)
    private String idProducto;

    @Column(length = 20)
    private String telefono;

    @Column(length = 80)
    private String referencia;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String moneda = "MXN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoTransaccion estado = EstadoTransaccion.PENDIENTE;

    @Column(nullable = false, length = 15)
    private String upc;

    @Column(nullable = false, length = 25)
    private String unidad;

    @Column(name = "hora_local", length = 14)
    private String horaLocal;

    @Column(name = "payload_enviado", columnDefinition = "TEXT")
    private String payloadEnviado;

    @Column(name = "codigo_proveedor", length = 10)
    private String codigoProveedor;

    @Column(name = "texto_proveedor", length = 500)
    private String textoProveedor;

    @Column(name = "id_tx_proveedor", length = 80)
    private String idTxProveedor;

    @Column(name = "num_autorizacion")
    private Long numAutorizacion;

    @Column(name = "intentos_conciliacion", nullable = false)
    @Builder.Default
    private Integer intentosConciliacion = 0;

    @Column(name = "fecha_envio_proveedor")
    private OffsetDateTime fechaEnvioProveedor;

    @Column(name = "fecha_confirmacion")
    private OffsetDateTime fechaConfirmacion;

    @Column(name = "fecha_ultima_conciliacion")
    private OffsetDateTime fechaUltimaConciliacion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    public boolean esTerminal() {
        return estado == EstadoTransaccion.APLICADA
                || estado == EstadoTransaccion.RECHAZADA
                || estado == EstadoTransaccion.NO_ENVIADA;
    }
}
