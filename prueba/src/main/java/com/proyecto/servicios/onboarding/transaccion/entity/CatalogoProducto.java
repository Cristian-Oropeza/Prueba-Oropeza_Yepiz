package com.proyecto.servicios.onboarding.transaccion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "catalogo_productos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_catalogo_productos_servicio_producto",
                columnNames = {"id_servicio", "id_producto"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_servicio", nullable = false, length = 50)
    private String idServicio;

    @Column(name = "id_producto", nullable = false, length = 50)
    private String idProducto;

    @Column(nullable = false, length = 200)
    private String descripcion;

    /** 1 = recarga (monto == precio); 2 = pago de servicio (referencia obligatoria). */
    @Column(name = "tipo_front", nullable = false)
    private Short tipoFront;

    /** 10 u 11 = canjeables (telefono "#1111111111"); otro valor = telefono normal. */
    @Column(name = "id_cat_tipo_servicio")
    private Short idCatTipoServicio;

    /** Para tipoFront=1: el monto de la transaccion debe ser exactamente este valor. */
    @Column(precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(name = "monto_min", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoMin;

    @Column(name = "monto_max", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoMax;

    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
    @Builder.Default
    private String moneda = "MXN";

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = Boolean.TRUE;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    public boolean esCanjeable() {
        return idCatTipoServicio != null
                && (idCatTipoServicio == 10 || idCatTipoServicio == 11);
    }

    public boolean esRecarga()        { return tipoFront != null && tipoFront == 1; }
    public boolean esPagoServicio()   { return tipoFront != null && tipoFront == 2; }
    public boolean montoEnRango(BigDecimal m) {
        return m != null && m.compareTo(montoMin) >= 0 && m.compareTo(montoMax) <= 0;
    }
}
