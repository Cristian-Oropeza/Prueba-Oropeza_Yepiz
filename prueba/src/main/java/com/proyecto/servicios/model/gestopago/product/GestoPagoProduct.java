package com.proyecto.servicios.model.gestopago.product;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;

import java.math.BigDecimal;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GestoPagoProduct {

    @XmlAttribute
    private String servicio;

    @XmlAttribute
    private String producto;

    @XmlAttribute
    private Integer idServicio;

    @XmlAttribute
    private Integer idProducto;

    @XmlAttribute
    private Integer idCatTipoServicio;

    @XmlAttribute
    private Integer tipoFront;

    @XmlAttribute
    private Boolean hasDigitoVerificador;

    @XmlAttribute
    private BigDecimal precio;

    @XmlAttribute
    private Boolean showAyuda;

    @XmlAttribute
    private String tipoReferencia;

    @XmlElement(name = "legend")
    @XmlJavaTypeAdapter(TrimmedStringAdapter.class)
    private String legend;
}
