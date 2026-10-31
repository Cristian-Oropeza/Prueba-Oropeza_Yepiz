package com.proyecto.servicios.onboarding.transaccion.repository;

import com.proyecto.servicios.onboarding.transaccion.entity.Transaccion;
import com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionRepository
        extends JpaRepository<Transaccion, Long>, JpaSpecificationExecutor<Transaccion> {

    Optional<Transaccion> findByIdempotencyKey(String idempotencyKey);
    Optional<Transaccion> findByReferenciaOperacion(String referenciaOperacion);

    /** Suma del monto de transacciones APLICADAS/EN_CONFIRMACION/PENDIENTES de una cuenta desde una fecha. */
    @Query("""
            SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t
            WHERE t.cuenta.id = :cuentaId
              AND t.estado IN :estados
              AND t.fechaCreacion >= :desde
            """)
    BigDecimal sumarMontoPorCuentaDesde(@Param("cuentaId") Long cuentaId,
                                        @Param("estados") List<EstadoTransaccion> estados,
                                        @Param("desde") OffsetDateTime desde);

    /** Cuenta transacciones recientes identicas (misma cuenta, producto y monto) para la ventana anti-duplicado. */
    @Query("""
            SELECT COUNT(t) FROM Transaccion t
            WHERE t.cuenta.id = :cuentaId
              AND t.idProducto = :idProducto
              AND t.monto = :monto
              AND t.fechaCreacion >= :desde
              AND t.estado IN :estadosActivos
            """)
    long contarAntiDuplicado(@Param("cuentaId") Long cuentaId,
                             @Param("idProducto") String idProducto,
                             @Param("monto") BigDecimal monto,
                             @Param("desde") OffsetDateTime desde,
                             @Param("estadosActivos") List<EstadoTransaccion> estadosActivos);

    /** Transacciones candidatas a conciliacion: EN_CONFIRMACION, enviadas en la ventana 62s-24h. */
    @Query("""
            SELECT t FROM Transaccion t
            WHERE t.estado = com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion.EN_CONFIRMACION
              AND t.fechaEnvioProveedor BETWEEN :desde AND :hasta
              AND t.intentosConciliacion < :maxIntentos
            ORDER BY t.fechaEnvioProveedor ASC
            """)
    List<Transaccion> findParaConciliar(@Param("desde") OffsetDateTime desde,
                                        @Param("hasta") OffsetDateTime hasta,
                                        @Param("maxIntentos") int maxIntentos,
                                        Pageable pageable);
}
