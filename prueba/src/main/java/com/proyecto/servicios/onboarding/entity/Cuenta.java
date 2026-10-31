package com.proyecto.servicios.onboarding.entity;

import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cuentas",
        uniqueConstraints = @UniqueConstraint(name = "uk_cuentas_numero", columnNames = "numero_cuenta"),
        indexes = {
                @Index(name = "idx_cuentas_cliente_id", columnList = "cliente_id"),
                @Index(name = "idx_cuentas_estatus",    columnList = "estatus")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, length = 18)
    private String numeroCuenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cuentas_cliente"))
    private Cliente cliente;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstatusCuenta estatus = EstatusCuenta.ACTIVA;

    /**
     * Columna de bloqueo optimista (JPA @Version). Impide que dos operaciones concurrentes sobre
     * la misma cuenta corrompan el saldo: Hibernate detecta el conflicto al hacer flush y lanza
     * ObjectOptimisticLockingFailureException, que el service de pagos atrapa y reintenta.
     * La columna fue agregada en V2__transacciones.sql con DEFAULT 0 para las cuentas existentes.
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "fecha_apertura", nullable = false, updatable = false)
    private OffsetDateTime fechaApertura;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;
}
