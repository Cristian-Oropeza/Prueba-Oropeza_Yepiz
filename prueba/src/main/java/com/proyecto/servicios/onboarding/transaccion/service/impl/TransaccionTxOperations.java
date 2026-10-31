package com.proyecto.servicios.onboarding.transaccion.service.impl;

import com.proyecto.servicios.onboarding.entity.Cliente;
import com.proyecto.servicios.onboarding.entity.Cuenta;
import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import com.proyecto.servicios.onboarding.exception.CuentaNoEncontradaException;
import com.proyecto.servicios.onboarding.repository.CuentaRepository;
import com.proyecto.servicios.onboarding.transaccion.client.dto.ResultadoTx;
import com.proyecto.servicios.onboarding.transaccion.dto.request.TransaccionEjecutarRequest;
import com.proyecto.servicios.onboarding.transaccion.entity.CatalogoProducto;
import com.proyecto.servicios.onboarding.transaccion.entity.Transaccion;
import com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion;
import com.proyecto.servicios.onboarding.transaccion.exception.CuentaNoActivaException;
import com.proyecto.servicios.onboarding.transaccion.exception.SaldoInsuficienteException;
import com.proyecto.servicios.onboarding.transaccion.exception.TransaccionNoEncontradaException;
import com.proyecto.servicios.onboarding.transaccion.repository.TransaccionRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Bean hermano del servicio orquestador. Agrupa las operaciones @Transactional REQUIRES_NEW para
 * que SI se abran transacciones nuevas: Spring solo aplica el proxy transaccional cuando la llamada
 * atraviesa un bean distinto. Si {@code apartarSaldo} viviera en TransaccionServiceImpl, el self-call
 * desde {@code ejecutar} seria sin proxy y degeneraria en una tx unica abierta durante la llamada
 * HTTP al proveedor.
 * <p>
 * Bloqueo <strong>optimista</strong> via la columna {@code version} de cuentas (anotada {@code @Version}
 * en {@link Cuenta}): si dos pagos simultaneos tocan la misma cuenta, Hibernate detecta el conflicto
 * al hacer flush y lanza {@link ObjectOptimisticLockingFailureException}; el service reintenta.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransaccionTxOperations {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    /**
     * TX corta: valida saldo bajo la cuenta actual, debita (version++) y crea Transaccion PENDIENTE.
     * Si otra tx cambio {@code version} de la cuenta mientras tanto, Hibernate lanzara
     * {@link ObjectOptimisticLockingFailureException} y el orquestador reintenta.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long apartarSaldoYCrearTransaccion(Cliente cliente, Cuenta cuentaIn, TransaccionEjecutarRequest req,
                                              CatalogoProducto producto, String referenciaOperacion,
                                              String upc, String telefonoFinal, String unidad) {
        // Releemos la cuenta DENTRO de la tx para tener la version actual.
        Cuenta cuenta = cuentaRepository.findById(cuentaIn.getId())
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada: " + cuentaIn.getId()));
        if (cuenta.getEstatus() != EstatusCuenta.ACTIVA) {
            throw new CuentaNoActivaException("La cuenta no esta ACTIVA (estatus=" + cuenta.getEstatus() + ")");
        }
        if (cuenta.getSaldo().compareTo(req.getMonto()) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente para la operacion");
        }
        BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(req.getMonto());
        cuenta.setSaldo(nuevoSaldo);
        cuentaRepository.save(cuenta); // trigger @Version check en flush

        Transaccion tx = Transaccion.builder()
                .referenciaOperacion(referenciaOperacion)
                .idempotencyKey(req.getIdempotencyKey())
                .cliente(cliente)
                .cuenta(cuenta)
                .idServicio(req.getIdServicio())
                .idProducto(req.getIdProducto())
                .telefono(telefonoFinal)
                .referencia(req.getReferencia())
                .monto(req.getMonto())
                .moneda(producto.getMoneda())
                .estado(EstadoTransaccion.PENDIENTE)
                .upc(upc)
                .unidad(unidad)
                .build();
        tx = transaccionRepository.save(tx);
        log.info("Saldo apartado cuenta={} nuevoSaldo={} tx_id={} ref={} upc={}",
                cuenta.getId(), nuevoSaldo, tx.getId(), referenciaOperacion, upc);
        return tx.getId();
    }

    /** TX corta: marca la transaccion como enviada al proveedor y registra el instante. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarEnviada(Long txId, String payloadEnviadoSanitizado, String horaLocal) {
        Transaccion t = transaccionRepository.findById(txId)
                .orElseThrow(() -> new TransaccionNoEncontradaException("id=" + txId));
        t.setFechaEnvioProveedor(OffsetDateTime.now());
        t.setPayloadEnviado(payloadEnviadoSanitizado);
        t.setHoraLocal(horaLocal);
        transaccionRepository.save(t);
    }

    /**
     * TX corta FINAL: aplica el resultado del proveedor:
     *   NO_ENVIADA → saldo devuelto, estado NO_ENVIADA.
     *   SIN_RESPUESTA (incluye codigo 82) → NO devolver saldo, estado EN_CONFIRMACION.
     *   RESPUESTA aprobada (NUM_AUTORIZACION > 0) → APLICADA, saldo queda descontado.
     *   RESPUESTA rechazada → RECHAZADA, saldo devuelto.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaccion aplicarResultadoEnvio(Long txId, ResultadoTx resultado) {
        Transaccion t = transaccionRepository.findById(txId)
                .orElseThrow(() -> new TransaccionNoEncontradaException("id=" + txId));

        if (!resultado.fueEnviada()) {
            devolverSaldo(t, "No se envio: " + sanitizar(resultado.detalle()));
            t.setEstado(EstadoTransaccion.NO_ENVIADA);
            t.setTextoProveedor(sanitizar(resultado.detalle()));
            return transaccionRepository.save(t);
        }

        if (resultado.tipo() == ResultadoTx.Tipo.SIN_RESPUESTA) {
            t.setEstado(EstadoTransaccion.EN_CONFIRMACION);
            t.setTextoProveedor(sanitizar(resultado.detalle()));
            log.warn("Tx id={} queda EN_CONFIRMACION: {}", txId, resultado.detalle());
            return transaccionRepository.save(t);
        }

        var xml = resultado.autorizacion();
        t.setCodigoProveedor(xml.getCodigoMensaje());
        t.setTextoProveedor(sanitizar(xml.getMensaje()));
        t.setIdTxProveedor(xml.getIdTransaccion());
        t.setNumAutorizacion(xml.getNumAutorizacion());
        t.setFechaConfirmacion(OffsetDateTime.now());

        if (xml.esAprobada()) {
            t.setEstado(EstadoTransaccion.APLICADA);
            log.info("Tx id={} APLICADA numAut={}", txId, xml.getNumAutorizacion());
            return transaccionRepository.save(t);
        }
        // Rechazo explicito (NUM_AUTORIZACION = -1 o nulo): devolver saldo.
        devolverSaldo(t, "Rechazo codigo=" + xml.getCodigoMensaje());
        t.setEstado(EstadoTransaccion.RECHAZADA);
        log.info("Tx id={} RECHAZADA codigo={} - saldo devuelto", txId, xml.getCodigoMensaje());
        return transaccionRepository.save(t);
    }

    /** TX corta: incrementa el contador de intentos de conciliacion. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementarIntentoConciliacion(Long txId) {
        Transaccion t = transaccionRepository.findById(txId)
                .orElseThrow(() -> new TransaccionNoEncontradaException("id=" + txId));
        t.setIntentosConciliacion(t.getIntentosConciliacion() + 1);
        t.setFechaUltimaConciliacion(OffsetDateTime.now());
        transaccionRepository.save(t);
    }

    /**
     * TX corta: aplica el resultado de confirmTx. Igual que aplicarResultadoEnvio pero NO reversa
     * cuando la respuesta sigue en "no sabemos" (deja la transaccion en EN_CONFIRMACION para el
     * proximo intento, hasta que caiga en la ventana > 24h).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaccion aplicarResultadoConciliacion(Long txId, ResultadoTx resultado) {
        Transaccion t = transaccionRepository.findById(txId)
                .orElseThrow(() -> new TransaccionNoEncontradaException("id=" + txId));
        if (t.getEstado() != EstadoTransaccion.EN_CONFIRMACION) {
            return t;
        }
        if (!resultado.fueEnviada() || resultado.tipo() == ResultadoTx.Tipo.SIN_RESPUESTA) {
            t.setTextoProveedor(sanitizar(resultado.detalle()));
            return transaccionRepository.save(t);
        }
        var xml = resultado.autorizacion();
        t.setCodigoProveedor(xml.getCodigoMensaje());
        t.setTextoProveedor(sanitizar(xml.getMensaje()));
        t.setIdTxProveedor(xml.getIdTransaccion());
        t.setNumAutorizacion(xml.getNumAutorizacion());
        t.setFechaConfirmacion(OffsetDateTime.now());
        if (xml.esAprobada()) {
            t.setEstado(EstadoTransaccion.APLICADA);
            log.info("Conciliacion tx id={} CONFIRMA APLICADA", txId);
            return transaccionRepository.save(t);
        }
        devolverSaldo(t, "Conciliacion rechaza codigo=" + xml.getCodigoMensaje());
        t.setEstado(EstadoTransaccion.RECHAZADA);
        log.info("Conciliacion tx id={} CONFIRMA RECHAZADA - saldo devuelto", txId);
        return transaccionRepository.save(t);
    }

    private void devolverSaldo(Transaccion t, String motivo) {
        Cuenta cuenta = cuentaRepository.findById(t.getCuenta().getId())
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada: " + t.getCuenta().getId()));
        BigDecimal nuevoSaldo = cuenta.getSaldo().add(t.getMonto());
        cuenta.setSaldo(nuevoSaldo);
        cuentaRepository.save(cuenta);
        log.info("Saldo devuelto cuenta={} nuevoSaldo={} tx_id={} motivo={}",
                cuenta.getId(), nuevoSaldo, t.getId(), motivo);
    }

    private static String sanitizar(String s) {
        if (s == null) return null;
        String limpio = s.replaceAll("(?i)bearer\\s+[A-Za-z0-9._-]+", "Bearer ***")
                         .replaceAll("(?i)password=\\S*", "password=***");
        return limpio.length() > 500 ? limpio.substring(0, 500) : limpio;
    }
}
