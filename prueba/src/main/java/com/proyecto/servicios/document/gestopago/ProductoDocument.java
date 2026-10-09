package com.proyecto.servicios.document.gestopago;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "gestopago_productos")
public class ProductoDocument {

    @Id
    private Integer idProducto;

    private String producto;

    private String servicio;

    private Integer idServicio;

    private Integer idCatTipoServicio;

    private Integer tipoFront;

    private Boolean hasDigitoVerificador;

    private BigDecimal precio;

    private Boolean showAyuda;

    private String tipoReferencia;

    private String legend;

    private LocalDateTime fechaSincronizacion;
}
