package com.proyecto.servicios.model.producto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProductoResponse {

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
