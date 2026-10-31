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
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Mantiene vigente el token Bearer de GestoPago. Endpoint real:
 *   POST /sistema/app/jwt-gp/authenticate/?idDistribuidor=&codigoDispositivo=&password=
 * (parametros como query, respuesta JSON con {token, expires_in, ...}).
 * <p>
 * Las credenciales del distribuidor 83 son publicas de la documentacion de PuntoRed; cada
 * distribuidor usa las suyas en produccion. El token vence a las 23h, se cachea y se renueva
 * reactivamente al recibir 401 desde el cliente de transacciones.
 * <p>
 * Thread-safe: ReentrantLock evita que N hilos simultaneos hagan login a la vez.
 */
@Component
@Slf4j
public class GestopagoAuthClient {

    private static final String PATH_LOGIN = "/sistema/app/jwt-gp/authenticate/";

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

    public Optional<String> obtenerTokenVigente() {
        if (tokenCache != null && expiraEn != null && clock.instant().isBefore(expiraEn)) {
            return Optional.of(tokenCache);
        }
        return renovar();
    }

    public Optional<String> renovar() {
        lock.lock();
        try {
            if (tokenCache != null && expiraEn != null && clock.instant().isBefore(expiraEn)) {
                return Optional.of(tokenCache);
            }
            if (props.getAuth().getPassword() == null || props.getAuth().getPassword().isBlank()) {
                log.error("No se puede renovar token GestoPago: GESTOPAGO_AUTH_PASSWORD no configurada");
                return Optional.empty();
            }
            try {
                AuthResponseXml resp = restClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path(PATH_LOGIN)
                                .queryParam("idDistribuidor",   props.getAuth().getIdDistribuidor())
                                .queryParam("codigoDispositivo", props.getAuth().getCodigoDispositivo())
                                .queryParam("password",         props.getAuth().getPassword())
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(AuthResponseXml.class);

                if (resp == null || resp.getToken() == null || resp.getToken().isBlank()) {
                    log.error("GestoPago login respondio sin token");
                    return Optional.empty();
                }
                this.tokenCache = resp.getToken().trim();
                // Si el proveedor devuelve expires_in (segundos), lo usamos; sino, vigencia configurada.
                long segundos = resp.getVigenciaHoras() != null
                        ? resp.getVigenciaHoras()
                        : props.getAuth().getVigencia().toSeconds();
                this.expiraEn = clock.instant().plusSeconds(segundos);
                log.info("Token GestoPago renovado. Vigente hasta: {}", expiraEn);
                return Optional.of(tokenCache);
            } catch (RestClientResponseException ex) {
                log.error("GestoPago login respondio HTTP {}", ex.getStatusCode().value());
                return Optional.empty();
            } catch (RuntimeException ex) {
                log.error("No se pudo renovar token GestoPago: {}", ex.getClass().getSimpleName());
                return Optional.empty();
            }
        } finally {
            lock.unlock();
        }
    }

    public void invalidar() {
        lock.lock();
        try {
            tokenCache = null;
            expiraEn = null;
        } finally {
            lock.unlock();
        }
    }
}
