package com.proyecto.servicios.model.producto;

import com.proyecto.servicios.model.GenericResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SincronizacionProductosResponse extends GenericResponse {

    private Integer totalProductos;
    private Long productosEliminados;
    private LocalDateTime fechaSincronizacion;
}
