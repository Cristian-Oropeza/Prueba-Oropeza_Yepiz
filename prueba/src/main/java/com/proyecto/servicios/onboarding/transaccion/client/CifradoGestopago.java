package com.proyecto.servicios.onboarding.transaccion.client;

import com.proyecto.servicios.onboarding.transaccion.config.GestopagoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Cifrado AES-256-CBC con PKCS5Padding para el payload de GestoPago. La llave (32 bytes) y el IV
 * (16 bytes) se reciben codificados en Base64 por variables de entorno. Si no estan configurados,
 * {@link #configurado()} devuelve false y las transacciones responden 503 sin intentar enviar nada
 * al proveedor (no hay riesgo de cargar al cliente).
 * <p>
 * Validamos al construir que los bytes decodificados tengan el tamanio correcto, para que una llave
 * mal pegada no cause fallas misteriosas hasta el primer pago en produccion.
 */
@Component
@Slf4j
public class CifradoGestopago {

    private static final String ALGORITMO = "AES/CBC/PKCS5Padding";
    private static final int BYTES_LLAVE  = 32; // AES-256
    private static final int BYTES_IV     = 16;

    private final byte[] llave;
    private final byte[] iv;

    public CifradoGestopago(GestopagoProperties props) {
        String llaveB64 = props.getCifrado().getLlave();
        String ivB64    = props.getCifrado().getIv();
        if (llaveB64 == null || llaveB64.isBlank() || ivB64 == null || ivB64.isBlank()) {
            log.warn("Cifrado GestoPago no configurado: no se podran cifrar payloads.");
            this.llave = null;
            this.iv = null;
            return;
        }
        byte[] k, i;
        try {
            k = Base64.getDecoder().decode(llaveB64.trim());
            i = Base64.getDecoder().decode(ivB64.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("GESTOPAGO_CIFRADO_LLAVE o _IV no son Base64 validos", ex);
        }
        if (k.length != BYTES_LLAVE) {
            throw new IllegalStateException("GESTOPAGO_CIFRADO_LLAVE debe ser de 32 bytes (AES-256). Recibidos: " + k.length);
        }
        if (i.length != BYTES_IV) {
            throw new IllegalStateException("GESTOPAGO_CIFRADO_IV debe ser de 16 bytes. Recibidos: " + i.length);
        }
        this.llave = k;
        this.iv    = i;
        log.info("Cifrado GestoPago inicializado (AES-256-CBC, PKCS5Padding).");
    }

    public boolean configurado() {
        return llave != null && iv != null;
    }

    /** Cifra el JSON en claro y lo devuelve en Base64. Usado para el campo "signed" del formulario. */
    public String cifrar(String plano) {
        if (!configurado()) {
            throw new IllegalStateException("Cifrado GestoPago no configurado");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(llave, "AES"), new IvParameterSpec(iv));
            byte[] enc = cipher.doFinal(plano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(enc);
        } catch (GeneralSecurityExceptionWrapper wrapper) {
            throw wrapper;
        } catch (Exception ex) {
            throw new GeneralSecurityExceptionWrapper("Error al cifrar payload de GestoPago", ex);
        }
    }

    /** Descifra Base64 → JSON en claro. Util para pruebas y para interpretar echoes del proveedor. */
    public String descifrar(String base64Cifrado) {
        if (!configurado()) {
            throw new IllegalStateException("Cifrado GestoPago no configurado");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(llave, "AES"), new IvParameterSpec(iv));
            byte[] dec = cipher.doFinal(Base64.getDecoder().decode(base64Cifrado));
            return new String(dec, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new GeneralSecurityExceptionWrapper("Error al descifrar respuesta de GestoPago", ex);
        }
    }

    /** RuntimeException para que el cliente HTTP pueda atraparla sin propagar checked exceptions. */
    public static class GeneralSecurityExceptionWrapper extends RuntimeException {
        public GeneralSecurityExceptionWrapper(String msg, Throwable cause) { super(msg, cause); }
    }
}
