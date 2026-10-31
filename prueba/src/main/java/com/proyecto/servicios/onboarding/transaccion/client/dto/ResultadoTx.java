package com.proyecto.servicios.onboarding.transaccion.client.dto;

import com.proyecto.servicios.onboarding.transaccion.client.xml.AutorizacionXml;

/**
 * Tres resultados posibles al invocar GestoPago, tal como propone el documento de integracion:
 *   NO_ENVIADA   : la peticion nunca salio (cifrado/token caidos). Seguro reversar el saldo.
 *   SIN_RESPUESTA: salio pero no se obtuvo XML interpretable (timeout, 5xx, body ilegible, codigo 82). DESCONOCIDO.
 *   RESPUESTA    : hubo XML interpretable (aprobada o rechazada explicita).
 */
public record ResultadoTx(AutorizacionXml autorizacion, String detalle, Tipo tipo) {

    public enum Tipo { RESPUESTA, SIN_RESPUESTA, NO_ENVIADA }

    public static ResultadoTx respuesta(AutorizacionXml xml)   { return new ResultadoTx(xml, null, Tipo.RESPUESTA); }
    public static ResultadoTx sinRespuesta(String detalle)     { return new ResultadoTx(null, detalle, Tipo.SIN_RESPUESTA); }
    public static ResultadoTx noEnviada(String detalle)        { return new ResultadoTx(null, detalle, Tipo.NO_ENVIADA); }

    public boolean tieneRespuesta() { return tipo == Tipo.RESPUESTA; }
    public boolean fueEnviada()     { return tipo != Tipo.NO_ENVIADA; }
    public boolean esAprobada()     { return tieneRespuesta() && autorizacion.esAprobada(); }
}
