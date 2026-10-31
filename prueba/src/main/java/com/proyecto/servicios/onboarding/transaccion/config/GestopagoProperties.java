package com.proyecto.servicios.onboarding.transaccion.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Configuracion de la integracion con GestoPago/PuntoRed. Alineado 1:1 con el documento oficial.
 * Variables sensibles (llave AES, IV, password) vienen SIEMPRE por variables de entorno,
 * nunca hardcodeadas en el repo.
 */
@ConfigurationProperties(prefix = "gestopago")
@Getter @Setter
@Slf4j
public class GestopagoProperties {

    /** Si false, los endpoints responden 503 sin tocar nada. */
    private boolean enabled = false;

    private Auth auth = new Auth();
    private Tx tx = new Tx();
    private Cifrado cifrado = new Cifrado();

    /** X-API-Key opcional del comercio. Puede quedar vacio. */
    private String apiKey = "";

    @Getter @Setter
    public static class Auth {
        private String url = "https://gestopago.portalventas.net";
        private String idDistribuidor = "83";
        private String codigoDispositivo = "GPS83-TPV-17";
        private String password = "";
        /** Reintento del token: cada hora se verifica si vencio. */
        private Duration refreshRate = Duration.ofHours(1);
        /** PuntoRed emite tokens con vigencia nominal de 23 horas. */
        private Duration vigencia = Duration.ofHours(23);
    }

    @Getter @Setter
    public static class Tx {
        private String url = "https://gestopago.portalventas.net";
        private String unidad = "083";
        private String zonaHoraria = "America/Mexico_City";
        /** Timeouts del cliente HTTP. Read debe ser > 62s porque GestoPago puede tardar hasta eso. */
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout    = Duration.ofSeconds(70);
        /** Monto maximo por una transaccion individual. */
        private BigDecimal montoMaximo = new BigDecimal("5000.00");
        /** Tope diario acumulado por cuenta. */
        private BigDecimal topeDiario  = new BigDecimal("20000.00");
        /** Ventana para la deteccion de duplicados sospechosos (misma cuenta+producto+monto). */
        private int ventanaAntiduplicadoSeg = 30;
        /** Scheduler: periodo entre corridas de conciliacion. */
        private Duration confirmacionIntervalo = Duration.ofMillis(60_000);
        /** Scheduler: segundos minimos desde el envio antes de llamar confirmTx (GestoPago exige >= 62). */
        private int minSegundosAntesDeConciliar = 62;
        /** Scheduler: horas maximas para dejar de intentar conciliacion (GestoPago no responde confirmTx despues de 24h). */
        private int maxHorasConciliar = 24;
        /** Scheduler: tope de intentos de conciliacion por transaccion antes de dejarla para revision manual. */
        private int maxIntentos = 60;
        /** Scheduler: tamanio del lote por corrida. */
        private int tamanoLote = 20;
        /** Si false, no corre el @Scheduled de conciliacion. */
        private boolean conciliacionEnabled = false;
    }

    @Getter @Setter
    public static class Cifrado {
        /** Llave AES-256 en Base64 (32 bytes). Entregada por PuntoRed. */
        private String llave = "";
        /** IV AES de 16 bytes en Base64. Entregado por PuntoRed. */
        private String iv = "";
    }

    @PostConstruct
    void validar() {
        if (!enabled) {
            log.warn("GestoPago DESHABILITADO (gestopago.enabled=false). Los endpoints /api/v1/transacciones/* responderan 503.");
            return;
        }
        if (parecePlaceholder(auth.url) || parecePlaceholder(tx.url)) {
            throw new IllegalStateException("gestopago.auth.url o tx.url contienen un placeholder sin resolver");
        }
        if (blankOrPlaceholder(cifrado.llave) || blankOrPlaceholder(cifrado.iv)) {
            log.warn("GestoPago modo real sin cifrado configurado (GESTOPAGO_CIFRADO_LLAVE/_IV). Las transacciones responderan 503 sin enviar nada al proveedor.");
        }
        if (blankOrPlaceholder(auth.password)) {
            log.warn("GestoPago sin GESTOPAGO_AUTH_PASSWORD. El login al proveedor fallara con 401.");
        }
        if (tx.unidad == null || tx.unidad.length() < 3 || tx.unidad.length() > 25) {
            throw new IllegalStateException("gestopago.tx.unidad debe tener de 3 a 25 caracteres");
        }
        log.info("GestoPago habilitado. auth.url={} tx.unidad={} readTimeout={} montoMax={} topeDiario={}",
                auth.url, tx.unidad, tx.readTimeout, tx.montoMaximo, tx.topeDiario);
    }

    private static boolean blankOrPlaceholder(String v) {
        return v == null || v.isBlank() || parecePlaceholder(v);
    }
    private static boolean parecePlaceholder(String v) {
        return v != null && v.contains("${") && v.contains("}");
    }
}
