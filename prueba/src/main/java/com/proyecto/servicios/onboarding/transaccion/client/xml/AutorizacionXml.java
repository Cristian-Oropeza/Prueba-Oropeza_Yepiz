package com.proyecto.servicios.onboarding.transaccion.client.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta XML de sendTx.do y confirmTx.do de GestoPago/PuntoRed. Estructura real:
 * <pre>{@code
 * <RESPONSE>
 *   <ID_TX>...</ID_TX>
 *   <NUM_AUTORIZACION>12345</NUM_AUTORIZACION>   -- "-1" = no aplicada; > 0 = aprobada
 *   <SALDO>...</SALDO> <COMISION>...</COMISION>
 *   <FECHA>...</FECHA> <MONTO>...</MONTO>
 *   <MENSAJE>
 *     <CODIGO>01</CODIGO>        -- "01" = exito en sendTx; "06" aplicada / "70" no aplicada en confirmTx
 *     <TEXTO>...</TEXTO>
 *     <REFERENCIA>...</REFERENCIA>
 *     <PIN>...</PIN>
 *     <ID_TX>...</ID_TX>         -- confirmTx devuelve el id aqui adentro
 *   </MENSAJE>
 * </RESPONSE>
 * }</pre>
 * Los numeros llegan como texto porque pueden venir con ceros a la izquierda ("01") o en notacion
 * cientifica (SALDO). Los getters-helper los parsean cuando aplica.
 */
@Getter @Setter @NoArgsConstructor
@XmlRootElement(name = "RESPONSE")
@XmlAccessorType(XmlAccessType.FIELD)
public class AutorizacionXml {

    @XmlElement(name = "ID_TX")
    private String idTx;

    @XmlElement(name = "NUM_AUTORIZACION")
    private String numAutorizacion;

    @XmlElement(name = "SALDO")
    private String saldo;

    @XmlElement(name = "COMISION")
    private String comision;

    @XmlElement(name = "FECHA")
    private String fecha;

    @XmlElement(name = "MONTO")
    private String monto;

    @XmlElement(name = "MENSAJE")
    private Mensaje mensaje;

    @Getter @Setter @NoArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Mensaje {
        @XmlElement(name = "CODIGO")     private String codigo;
        @XmlElement(name = "TEXTO")      private String texto;
        @XmlElement(name = "REFERENCIA") private String referencia;
        @XmlElement(name = "PIN")        private String pin;
        @XmlElement(name = "legend")     private String legend;
        @XmlElement(name = "ID_TX")      private String idTx;
    }

    /** Codigo del proveedor ("01" exito, "03" carrier no responde, "82" sin certeza, "-1" no aplicada). */
    public String codigo() {
        return mensaje != null && mensaje.getCodigo() != null ? mensaje.getCodigo().trim() : null;
    }

    public String texto() {
        return mensaje != null && mensaje.getTexto() != null ? mensaje.getTexto().trim() : null;
    }

    /** ID_TX viene en la raiz (sendTx) o dentro de MENSAJE (confirmTx). */
    public String idTransaccion() {
        if (idTx != null && !idTx.isBlank()) return idTx.trim();
        return mensaje != null && mensaje.getIdTx() != null ? mensaje.getIdTx().trim() : null;
    }

    /** NUM_AUTORIZACION parseado como Long. null si no es parseable. */
    public Long numAutorizacionLong() {
        if (numAutorizacion == null || numAutorizacion.isBlank()) return null;
        try { return Long.parseLong(numAutorizacion.trim()); }
        catch (NumberFormatException ex) { return null; }
    }

    /** Aprobada si CODIGO = "01" (sendTx) o "06" (confirmTx) y NUM_AUTORIZACION > 0. */
    public boolean esAprobada() {
        String c = codigo();
        Long n = numAutorizacionLong();
        boolean codigoOk = "01".equals(c) || "06".equals(c);
        boolean numOk    = n != null && n > 0L;
        return codigoOk && numOk;
    }
}
