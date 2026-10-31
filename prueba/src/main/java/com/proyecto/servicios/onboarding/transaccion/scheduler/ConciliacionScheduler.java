package com.proyecto.servicios.onboarding.transaccion.scheduler;

import com.proyecto.servicios.onboarding.transaccion.client.GestopagoTxGateway;
import com.proyecto.servicios.onboarding.transaccion.client.dto.PayloadSendTx;
import com.proyecto.servicios.onboarding.transaccion.client.dto.ResultadoTx;
import com.proyecto.servicios.onboarding.transaccion.config.GestopagoProperties;
import com.proyecto.servicios.onboarding.transaccion.entity.Transaccion;
import com.proyecto.servicios.onboarding.transaccion.repository.TransaccionRepository;
import com.proyecto.servicios.onboarding.transaccion.service.impl.TransaccionTxOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Reintenta la conciliacion de transacciones en EN_CONFIRMACION cada 60s. GestoPago exige llamar a
 * confirmTx en una ventana precisa: NO antes de 62s desde el envio y NO despues de 24h. Fuera de esa
 * ventana el proveedor no responde confirmTx y la transaccion queda para revision manual.
 * <p>
 * Activo solo si {@code gestopago.tx.conciliacion-enabled=true}; por defecto esta apagado para que
 * no dispare llamadas al proveedor sin que el operador lo decida.
 */
@Component
@ConditionalOnProperty(prefix = "gestopago.tx", name = "conciliacion-enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ConciliacionScheduler {

    private final TransaccionRepository transaccionRepository;
    private final TransaccionTxOperations tx;
    private final GestopagoTxGateway gateway;
    private final GestopagoProperties props;
    private final Clock clock;

    @Scheduled(fixedDelayString = "#{@gestopagoProperties.tx.confirmacionIntervalo.toMillis()}",
               initialDelay = 30_000)
    public void conciliarLote() {
        OffsetDateTime ahora = OffsetDateTime.now(clock);
        OffsetDateTime hasta = ahora.minusSeconds(props.getTx().getMinSegundosAntesDeConciliar());
        OffsetDateTime desde = ahora.minusHours(props.getTx().getMaxHorasConciliar());

        List<Transaccion> pendientes = transaccionRepository.findParaConciliar(
                desde, hasta, props.getTx().getMaxIntentos(),
                PageRequest.of(0, props.getTx().getTamanoLote()));

        if (pendientes.isEmpty()) return;
        log.info("Conciliacion: {} transacciones a procesar", pendientes.size());
        for (Transaccion t : pendientes) {
            try {
                tx.incrementarIntentoConciliacion(t.getId());
                PayloadSendTx payload = PayloadSendTx.builder()
                        .idProducto(t.getIdProducto())
                        .idServicio(t.getIdServicio())
                        .telefono(t.getTelefono())
                        .horaLocal(t.getHoraLocal())
                        .referencia(t.getReferencia())
                        .montoPago(t.getMonto() == null ? BigDecimal.ZERO : t.getMonto())
                        .upc(t.getUpc())
                        .unidad(t.getUnidad())
                        .build();
                ResultadoTx resultado = gateway.confirmar(payload);
                tx.aplicarResultadoConciliacion(t.getId(), resultado);
            } catch (RuntimeException ex) {
                log.warn("Fallo al conciliar tx id={}: {}", t.getId(), ex.getMessage());
            }
        }
    }
}
