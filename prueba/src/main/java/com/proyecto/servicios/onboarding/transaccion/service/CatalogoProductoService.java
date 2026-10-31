package com.proyecto.servicios.onboarding.transaccion.service;

import com.proyecto.servicios.onboarding.transaccion.entity.CatalogoProducto;

public interface CatalogoProductoService {
    CatalogoProducto obtenerActivoOExcepcion(String idServicio, String idProducto);
}
