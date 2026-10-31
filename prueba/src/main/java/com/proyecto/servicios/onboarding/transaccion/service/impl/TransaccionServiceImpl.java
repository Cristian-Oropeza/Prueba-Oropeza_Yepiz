package com.proyecto.servicios.onboarding.transaccion.service.impl;

import com.proyecto.servicios.onboarding.entity.Cliente;
import com.proyecto.servicios.onboarding.entity.Cuenta;
import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import com.proyecto.servicios.onboarding.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.onboarding.exception.CuentaNoEncontradaException;
import com.proyecto.servicios.onboarding.repository.ClienteRepository;
import com.proyecto.servicios.onboarding.repository.CuentaRepository;
import com.proyecto.servicios.onboarding.transaccion.client.GestopagoTxGateway;
import com.proyecto.servicios.onboarding.transaccion.client.dto.PayloadSendTx;
import com.proyecto.servicios.onboarding.transaccion.client.dto.ResultadoTx;
import com.proyecto.servicios.onboarding.transaccion.config.GestopagoProperties;
import com.proyecto.servicios.onboarding.transaccion.dto.request.TransaccionConsultaRequest;
import com.proyecto.servicios.onboarding.transaccion.dto.request.TransaccionEjecutarRequest;
import com.proyecto.servicios.onboarding.transaccion.dto.response.TransaccionResponse;
import com.proyecto.servicios.onboarding.transaccion.entity.CatalogoProducto;
import com.proyecto.servicios.onboarding.transaccion.entity.Transaccion;
import com.proyecto.servicios.onboarding.transaccion.exception.CuentaNoActivaException;
import com.proyecto.servicios.onboarding.transaccion.exception.CuentaNoPerteneceAlClienteException;
import com.proyecto.servicios.onboarding.transaccion.exception.LimiteExcedidoException;
import com.proyecto.servicios.onboarding.transaccion.exception.MontoFueraDeRangoException;
import com.proyecto.servicios.onboarding.transaccion.exception.ProveedorNoDisponibleException;
import com.proyecto.servicios.onboarding.transaccion.exception.ReglaProductoViolatedException;
import com.proyecto.servicios.onboarding.transaccion.exception.TransaccionDuplicadaException;
import com.proyecto.servicios.onboarding.transaccion.exception.TransaccionNoEncontradaException;
import com.proyecto.servicios.onboarding.transaccion.mapper.TransaccionMapper;
import com.proyecto.servicios.onboarding.transaccion.repository.TransaccionRepository;
import com.proyecto.servicios.onboarding.transaccion.service.CatalogoProductoService;
import com.proyecto.servicios.onboarding.transaccion.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Orquestacion de transacciones GestoPago alineada con el documento de integracion.
 * <p>
 * Flujo de {@link #ejecutar(TransaccionEjecutarRequest)}:
 * <ol>
 *   <li>Verificar modulo habilitado (503 si gestopago.enabled=false).</li>
 *   <li>Idempotencia por idempotency_key (UNIQUE a nivel BD).</li>
 *   <li>Resolver cliente + cuenta + validar propiedad (cuenta pertenece al cliente).</li>
 *   <li>Resolver producto del catalogo + validar reglas de tipoFront.</li>
 *   <li>Validar limites: monto maximo, tope diario, ventana anti-duplicado.</li>
 *   <li>TX #1 (REQUIRES_NEW): apartar saldo (bloqueo optimista) + crear Transaccion PENDIENTE.
 *       Reintento hasta 3 veces si colisiona la version.</li>
 *   <li>Marcar enviada + llamar a GestoPago (fuera de TX, con payload cifrado AES).</li>
 *   <li>TX #2 (REQUIRES_NEW): aplicar resultado (APLICADA | EN_CONFIRMACION | RECHAZADA | NO_ENVIADA).</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransaccionServiceImpl implements TransaccionService {

    private static final String ALFABETO_UPC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGITUD_UPC = 10;
    private static final String TELEFONO_CANJEABLE = "#1111111111";
    private static final String TELEFONO_SERVICIO  = "1111111111";
    private static final DateTimeFormatter FORMATO_HORA_LOCAL = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_REINTENTOS_OPTIMISTA = 3;
    private static final List<com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion> ESTADOS_ACTIVOS = List.of(
            com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion.PENDIENTE,
            com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion.EN_CONFIRMACION,
            com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion.APLICADA);

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final CatalogoProductoService catalogoService;
    private final GestopagoTxGateway gateway;
    private final GestopagoProperties props;
    private final TransaccionMapper mapper;
    private final TransaccionTxOperations tx;
    private final Clock clock;

    @Override
    public TransaccionResponse ejecutar(TransaccionEjecutarRequest req) {
        if (!props.isEnabled()) {
            throw new ProveedorNoDisponibleException("Modulo de pagos deshabilitado (gestopago.enabled=false)");
        }

        // 1. Idempotencia (barrera previa; la UNIQUE de BD es el seguro de ultima instancia).
        var previa = transaccionRepository.findByIdempotencyKey(req.getIdempotencyKey());
        if (previa.isPresent()) {
            log.info("Idempotencia: devolviendo tx previa id={} ref={}", previa.get().getId(), previa.get().getReferenciaOperacion());
            return mapper.toResponse(previa.get());
        }

        // 2. Resolver cliente y cuenta.
        Cliente cliente = clienteRepository.findById(req.getClienteId())
                .orElseThrow(() -> ClienteNoEncontradoException.porId(req.getClienteId()));
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(req.getNumeroCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada: " + req.getNumeroCuenta()));
        if (!cuenta.getCliente().getId().equals(cliente.getId())) {
            log.warn("Cliente id={} intento operar con cuenta ajena numero={}", cliente.getId(), req.getNumeroCuenta());
            throw new CuentaNoPerteneceAlClienteException("La cuenta no pertenece al cliente autenticado");
        }
        if (cuenta.getEstatus() != EstatusCuenta.ACTIVA) {
            throw new CuentaNoActivaException("Cuenta en estatus " + cuenta.getEstatus());
        }

        // 3. Catalogo + reglas de tipoFront.
        CatalogoProducto producto = catalogoService.obtenerActivoOExcepcion(req.getIdServicio(), req.getIdProducto());
        if (!producto.montoEnRango(req.getMonto())) {
            throw new MontoFueraDeRangoException("Monto " + req.getMonto() +
                    " fuera del rango [" + producto.getMontoMin() + ", " + producto.getMontoMax() + "]");
        }
        String telefonoFinal = aplicarReglasProducto(req, producto);

        // 4. Limites y anti-duplicado.
        validarLimites(cuenta, req);
        validarAntiDuplicado(cuenta, req);

        // 5. TX #1 con reintento en caso de choque optimista.
        String referenciaOperacion = generarReferenciaOperacion();
        String upc = generarUpc();
        Long txId = apartarConReintento(cliente, cuenta, req, producto, referenciaOperacion, upc, telefonoFinal);

        // 6. Enviar a GestoPago (fuera de TX).
        String horaLocal = FORMATO_HORA_LOCAL.format(
                LocalDateTime.now(clock.withZone(ZoneId.of(props.getTx().getZonaHoraria()))));
        PayloadSendTx payload = construirPayload(req, upc, telefonoFinal, horaLocal);
        tx.marcarEnviada(txId, "payload cifrado AES-256-CBC", horaLocal);
        ResultadoTx resultado = gateway.enviar(payload);

        // 7. Aplicar resultado.
        Transaccion finalizada = tx.aplicarResultadoEnvio(txId, resultado);
        return mapper.toResponse(finalizada);
    }

    @Override
    public TransaccionResponse consultar(TransaccionConsultaRequest req) {
        Transaccion t = transaccionRepository.findByReferenciaOperacion(req.getReferenciaOperacion())
                .orElseThrow(() -> new TransaccionNoEncontradaException(
                        "No existe transaccion con referencia " + req.getReferenciaOperacion()));
        return mapper.toResponse(t);
    }

    // ------------------------------------------------------------------------------------------------
    //  Reglas de tipoFront (segun doc PuntoRed)
    // ------------------------------------------------------------------------------------------------
    private String aplicarReglasProducto(TransaccionEjecutarRequest req, CatalogoProducto p) {
        if (p.esRecarga()) {
            // tipoFront=1: el monto debe ser EXACTAMENTE el precio del producto cuando hay precio fijo.
            if (p.getPrecio() != null && p.getPrecio().compareTo(req.getMonto()) != 0) {
                throw new ReglaProductoViolatedException(
                        "El monto debe ser exactamente " + p.getPrecio() + " para este producto");
            }
            // Canjeables: telefono fijo "#1111111111".
            if (p.esCanjeable()) return TELEFONO_CANJEABLE;
            // No canjeables: telefono obligatorio de 10 digitos.
            if (req.getTelefono() == null || !req.getTelefono().matches("^[0-9]{10}$")) {
                throw new ReglaProductoViolatedException(
                        "El telefono (10 digitos) es obligatorio para este producto");
            }
            return req.getTelefono();
        }
        if (p.esPagoServicio()) {
            // tipoFront=2: la referencia es obligatoria; telefono fijo "1111111111".
            if (req.getReferencia() == null || req.getReferencia().isBlank()) {
                throw new ReglaProductoViolatedException(
                        "La referencia es obligatoria para pagos de servicio");
            }
            return TELEFONO_SERVICIO;
        }
        throw new ReglaProductoViolatedException("tipoFront desconocido: " + p.getTipoFront());
    }

    // ------------------------------------------------------------------------------------------------
    //  Limites de monto / tope diario / anti-duplicado
    // ------------------------------------------------------------------------------------------------
    private void validarLimites(Cuenta cuenta, TransaccionEjecutarRequest req) {
        BigDecimal montoMax = props.getTx().getMontoMaximo();
        if (req.getMonto().compareTo(montoMax) > 0) {
            throw new LimiteExcedidoException(
                    "El monto excede el maximo por transaccion (" + montoMax + ")");
        }
        OffsetDateTime inicioDia = OffsetDateTime.now(clock).toLocalDate()
                .atStartOfDay(clock.getZone()).toOffsetDateTime();
        BigDecimal acumulado = transaccionRepository.sumarMontoPorCuentaDesde(
                cuenta.getId(), ESTADOS_ACTIVOS, inicioDia);
        if (acumulado.add(req.getMonto()).compareTo(props.getTx().getTopeDiario()) > 0) {
            throw new LimiteExcedidoException(
                    "Se superaria el tope diario (" + props.getTx().getTopeDiario() +
                    "). Acumulado hoy: " + acumulado);
        }
    }

    private void validarAntiDuplicado(Cuenta cuenta, TransaccionEjecutarRequest req) {
        OffsetDateTime desde = OffsetDateTime.now(clock)
                .minusSeconds(props.getTx().getVentanaAntiduplicadoSeg());
        long iguales = transaccionRepository.contarAntiDuplicado(
                cuenta.getId(), req.getIdProducto(), req.getMonto(), desde, ESTADOS_ACTIVOS);
        if (iguales > 0) {
            throw new TransaccionDuplicadaException(
                    "Operacion identica detectada en los ultimos " +
                    props.getTx().getVentanaAntiduplicadoSeg() + " segundos");
        }
    }

    // ------------------------------------------------------------------------------------------------
    //  Reserva de saldo con reintento optimista
    // ------------------------------------------------------------------------------------------------
    private Long apartarConReintento(Cliente cliente, Cuenta cuenta, TransaccionEjecutarRequest req,
                                     CatalogoProducto producto, String ref, String upc, String telefonoFinal) {
        int intento = 0;
        while (true) {
            try {
                return tx.apartarSaldoYCrearTransaccion(cliente, cuenta, req, producto, ref, upc,
                        telefonoFinal, props.getTx().getUnidad());
            } catch (ObjectOptimisticLockingFailureException ex) {
                intento++;
                if (intento >= MAX_REINTENTOS_OPTIMISTA) {
                    log.warn("Reserva de saldo fallo despues de {} reintentos por conflicto optimista", MAX_REINTENTOS_OPTIMISTA);
                    throw new com.proyecto.servicios.onboarding.transaccion.exception.ConcurrenciaException("Operacion no pudo completarse por conflicto de concurrencia, por favor reintentelo");
                }
                log.info("Reserva de saldo colisiono (version), reintento #{}", intento);
                try { Thread.sleep(50L * intento); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------------------------------------
    private PayloadSendTx construirPayload(TransaccionEjecutarRequest req, String upc,
                                           String telefonoFinal, String horaLocal) {
        return PayloadSendTx.builder()
                .idProducto(req.getIdProducto())
                .idServicio(req.getIdServicio())
                .telefono(telefonoFinal)
                .horaLocal(horaLocal)
                .referencia(req.getReferencia())
                .montoPago(req.getMonto())
                .upc(upc)
                .unidad(props.getTx().getUnidad())
                .build();
    }

    static String generarReferenciaOperacion() {
        return "OP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    static String generarUpc() {
        StringBuilder sb = new StringBuilder(LONGITUD_UPC);
        for (int i = 0; i < LONGITUD_UPC; i++) {
            sb.append(ALFABETO_UPC.charAt(RANDOM.nextInt(ALFABETO_UPC.length())));
        }
        return sb.toString();
    }
}
