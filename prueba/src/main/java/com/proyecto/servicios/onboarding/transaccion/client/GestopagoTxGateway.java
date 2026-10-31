package com.proyecto.servicios.onboarding.transaccion.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.onboarding.transaccion.client.dto.PayloadSendTx;
import com.proyecto.servicios.onboarding.transaccion.client.dto.ResultadoTx;
import com.proyecto.servicios.onboarding.transaccion.client.xml.AutorizacionXml;
import com.proyecto.servicios.onboarding.transaccion.config.GestopagoProperties;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.time.Clock;
import java.util.function.Function;

/**
 * Fachada de GestoPago para sendTx/confirmTx. Pasos del flujo:
 *   1. Cifrar el payload JSON con AES-256-CBC -> Base64 -> campo "signed".
 *   2. Formulario x-www-form-urlencoded con "random" (timestamp) + "signed".
 *   3. Header Authorization: Bearer <token-vigente> (renueva automatico en 401/403).
 *   4. Respuesta XML &lt;AUTORIZACION&gt;, parseada con JAXB.
 * <p>
 * Nunca propaga excepciones de red: todo caso regresa un {@link ResultadoTx} tipificado para
 * que el service decida saldo/estado sin tener que distinguir timeouts de errores de parsing.
 * El token y el password jamas salen en logs.
 */
@Component
@Slf4j
public class GestopagoTxGateway {

    private static final String PATH_SEND_TX    = "/sistema/service/sendTx.do";
    private static final String PATH_CONFIRM_TX = "/sistema/service/confirmTx.do";

    private final RestClient txRestClient;
    private final GestopagoAuthClient authClient;
    private final CifradoGestopago cifrado;
    private final ObjectMapper objectMapper;
    private final GestopagoProperties props;
    private final Clock clock;
    private final JAXBContext jaxbContext;

    public GestopagoTxGateway(@Qualifier("gestopagoTxRestClient") RestClient txRestClient,
                              GestopagoAuthClient authClient,
                              CifradoGestopago cifrado,
                              ObjectMapper objectMapper,
                              GestopagoProperties props,
                              Clock clock) throws JAXBException {
        this.txRestClient = txRestClient;
        this.authClient = authClient;
        this.cifrado = cifrado;
        this.objectMapper = objectMapper;
        this.props = props;
        this.clock = clock;
        // JAXBContext es caro de construir pero thread-safe para reutilizar.
        this.jaxbContext = JAXBContext.newInstance(AutorizacionXml.class);
    }

    public ResultadoTx enviar(PayloadSendTx payload) {
        return ejecutar(PATH_SEND_TX, payload);
    }

    public ResultadoTx confirmar(PayloadSendTx payload) {
        return ejecutar(PATH_CONFIRM_TX, payload);
    }

    private ResultadoTx ejecutar(String path, PayloadSendTx payload) {
        if (!cifrado.configurado()) {
            return ResultadoTx.noEnviada("Cifrado GestoPago no configurado");
        }
        // 1. Serializar + cifrar
        String signed;
        try {
            String json = objectMapper.writeValueAsString(payload);
            signed = cifrado.cifrar(json);
        } catch (JsonProcessingException | RuntimeException ex) {
            log.error("GestoPago {} no se envio: fallo al armar payload - {}", path, ex.getMessage());
            return ResultadoTx.noEnviada("Error al armar payload: " + ex.getClass().getSimpleName());
        }

        // 2. Token vigente
        String token = authClient.obtenerTokenVigente().orElse(null);
        if (token == null) {
            return ResultadoTx.noEnviada("Sin token vigente de GestoPago");
        }

        // 3. Formulario + llamada; reintento una vez si 401/403
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("random", String.valueOf(clock.millis()));
        form.add("signed", signed);

        try {
            String xmlBody = llamar(path, form, "Bearer " + token);
            return interpretarXml(path, xmlBody);
        } catch (TokenRechazadoException ex) {
            // 401/403: renovamos token y reintentamos una vez
            log.warn("GestoPago {} rechazo token; renovando y reintentando", path);
            String nuevo = authClient.renovar().orElse(null);
            if (nuevo == null) {
                return ResultadoTx.noEnviada("No se pudo renovar token tras 401/403");
            }
            try {
                String xmlBody = llamar(path, form, "Bearer " + nuevo);
                return interpretarXml(path, xmlBody);
            } catch (TokenRechazadoException second) {
                return ResultadoTx.noEnviada("Token GestoPago rechazado incluso tras renovar");
            } catch (SinRespuestaException second) {
                return ResultadoTx.sinRespuesta(second.getMessage());
            }
        } catch (SinRespuestaException ex) {
            return ResultadoTx.sinRespuesta(ex.getMessage());
        }
    }

    private String llamar(String path, LinkedMultiValueMap<String, String> form, String authHeader) {
        try {
            RestClient.RequestBodySpec req = txRestClient.post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.ALL);
            String apiKey = props.getApiKey();
            if (apiKey != null && !apiKey.isBlank()) {
                req.header("X-API-Key", apiKey);
            }
            return req.body(form).retrieve().body(String.class);
        } catch (RestClientResponseException ex) {
            int s = ex.getStatusCode().value();
            if (s == HttpStatus.UNAUTHORIZED.value() || s == HttpStatus.FORBIDDEN.value()) {
                throw new TokenRechazadoException();
            }
            log.error("GestoPago {} respondio HTTP {}", path, s);
            throw new SinRespuestaException("Proveedor respondio HTTP " + s);
        } catch (ResourceAccessException ex) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                log.error("GestoPago {} timeout: {}", path, ex.getMessage());
                throw new SinRespuestaException("Timeout del proveedor");
            }
            log.error("GestoPago {} sin respuesta: {}", path, ex.getClass().getSimpleName());
            throw new SinRespuestaException("Sin respuesta: " + ex.getClass().getSimpleName());
        } catch (RuntimeException ex) {
            log.error("GestoPago {} error inesperado: {}", path, ex.getMessage());
            throw new SinRespuestaException("Error inesperado: " + ex.getClass().getSimpleName());
        }
    }

    private ResultadoTx interpretarXml(String path, String xmlBody) {
        if (xmlBody == null || xmlBody.isBlank()) {
            return ResultadoTx.sinRespuesta("Cuerpo XML vacio");
        }
        try {
            AutorizacionXml xml = (AutorizacionXml) jaxbContext.createUnmarshaller()
                    .unmarshal(new StringReader(xmlBody));
            log.info("GestoPago {} respondio codigo={} numAut={}",
                    path, xml.codigo(), xml.numAutorizacionLong());
            return ResultadoTx.respuesta(xml);
        } catch (JAXBException ex) {
            log.error("No se pudo parsear XML de GestoPago {}: {}", path, ex.getMessage());
            return ResultadoTx.sinRespuesta("XML no reconocido");
        }
    }

    private static class TokenRechazadoException extends RuntimeException {}
    private static class SinRespuestaException extends RuntimeException {
        SinRespuestaException(String msg) { super(msg); }
    }
}
