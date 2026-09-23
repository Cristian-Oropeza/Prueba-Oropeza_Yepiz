package com.proyecto.servicios.model.gestopago.product;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GestoPagoMessage {

    private static final String SUCCESS_CODE = "01";

    @XmlElement(name = "CODIGO")
    private String codigo;

    @XmlElement(name = "TEXTO")
    private String texto;

    public boolean isExitoso() {
        return SUCCESS_CODE.equals(codigo);
    }
}