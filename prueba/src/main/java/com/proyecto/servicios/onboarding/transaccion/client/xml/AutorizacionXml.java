package com.proyecto.servicios.onboarding.transaccion.client.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 * Respuesta XML de GestoPago para sendTx/confirmTx. El nodo raiz REAL es {@code <AUTORIZACION>}
 * (no RESPONSE); el campo clave es {@code NUM_AUTORIZACION} donde un valor > 0 indica aprobada
 * y -1 (o ausencia) indica no aplicada. {@code CODIGO_MENSAJE} trae codigos como "01" (ok),
 * "03" (carrier no responde), etc.
 */
@XmlRootElement(name = "AUTORIZACION")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter @Setter
public class AutorizacionXml {

    @XmlElement(name = "NUM_AUTORIZACION")
    private Long numAutorizacion;

    @XmlElement(name = "CODIGO_MENSAJE")
    private String codigoMensaje;

    @XmlElement(name = "MENSAJE")
    private String mensaje;

    @XmlElement(name = "ID_TRANSACCION")
    private String idTransaccion;

    @XmlElement(name = "UPC")
    private String upc;

    @XmlElement(name = "FECHA_HORA")
    private String fechaHora;

    public boolean esAprobada() {
        return numAutorizacion != null && numAutorizacion > 0L;
    }
}
