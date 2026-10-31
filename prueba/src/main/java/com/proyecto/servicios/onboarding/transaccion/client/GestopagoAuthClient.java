package com.proyecto.servicios.onboarding.transaccion.client;

import com.proyecto.servicios.onboarding.transaccion.client.xml.AuthResponseXml;
import com.proyecto.servicios.onboarding.transaccion.config.GestopagoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Mantiene vigente el token Bearer de GestoPago. Hace login con id-distribuidor + codigo-dispositivo
 * + password (credenciales del distribuidor 83 son publicas de la documentacion de PuntoRed; cada
 * distribuidor usa las suyas en produccion), cachea el token y renueva proactivamente antes de que
 * expire (23h) o reactivamente al recibir 401 desde el cliente de transacciones.
 * <p>
 * Thread-safe: usa un ReentrantLock para evitar que N hilos simultaneos hagan login a la vez.
 */
@Component
@Slf4j
public class GestopagoAuthClient {

    private final RestClient restClient;
    private final GestopagoProperties props;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile String tokenCache;
    private volatile Instant expiraEn;

    public GestopagoAuthClient(@Qualifier("gestopagoAuthRestClient") RestClient restClient,
                               GestopagoProperties props,
                               Clock clock) {
        this.restClient = restClient;
        this.props = props;
        this.clock = clock;
    }

    /** Devuelve un token vigente. Si no hay, se intenta obtener uno nuevo. Empty si falla. */
    public Optional<String> obtenerTokenVigente() {
        if (tokenCache != null && expiraEn != null && clock.instant().isBefore(expiraEn)) {
            return Optional.of(tokenCache);
        }
        return renovar();
    }

    /** Forza la renovacion del token. Util cuando el cliente de tx recibe 401. */
    public Optional<String> renovar() {
        lock.lock();
        try {
            // Doble chequeo: si otro hilo renovo mientras esperabamos el lock, aprovechamos.
            if (tokenCache != null && expiraEn != null && clock.instant().isBefore(expiraEn)) {
                return Optional.of(tokenCache);
            }
            if (props.getAuth().getPassword() == null || props.getAuth().getPassword().isBlank()) {
                log.error("No se puede renovar token GestoPago: GESTOPAGO_AUTH_PASSWORD no configurada");
                return Optional.empty();
            }
            try {
                Map<String, String> body = new LinkedHashMap<>();
                body.put("idDistribuidor",   props.getAuth().getIdDistribuidor());
                body.put("codigoDispositivo", props.getAuth().getCodigoDispositivo());
                body.put("password",         props.getAuth().getPassword());

                AuthResponseXml resp = restClient.post()
                        .uri("/sistema/service/login.do")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                        .body(toFormMap(body))
                        .retrieve()
                        .body(AuthResponseXml.class);

                if (resp == null || resp.getToken() == null || resp.getToken().isBlank()) {
                    log.error("GestoPago login respondio sin token");
                    return Optional.empty();
                }
                this.tokenCache = resp.getToken().trim();
                long horas = resp.getVigenciaHoras() != null ? resp.getVigenciaHoras() : props.getAuth().getVigencia().toHours();
                this.expiraEn = clock.instant().plusSeconds(horas * 3600);
                log.info("Token GestoPago renovado. Vigente hasta: {}", expiraEn);
                return Optional.of(tokenCache);
            } catch (RestClientResponseException ex) {
                log.error("GestoPago login respondio HTTP {}: {}", ex.getStatusCode(), sanitizar(ex.getMessage()));
                return Optional.empty();
            } catch (RuntimeException ex) {
                log.error("No se pudo renovar token GestoPago: {}", sanitizar(ex.getMessage()));
                return Optional.empty();
            }
        } finally {
            lock.unlock();
        }
    }

    /** Invalida la cache para forzar el proximo login. Util para tests y para reset manual. */
    public void invalidar() {
        lock.lock();
        try {
            tokenCache = null;
            expiraEn = null;
        } finally {
            lock.unlock();
        }
    }

    private static org.springframework.util.LinkedMultiValueMap<String, String> toFormMap(Map<String, String> body) {
        var mv = new org.springframework.util.LinkedMultiValueMap<String, String>();
        body.forEach(mv::add);
        return mv;
    }

    private static String sanitizar(String s) {
        if (s == null) return null;
        return s.replaceAll("(?i)password=\\S*", "password=***");
    }
}
