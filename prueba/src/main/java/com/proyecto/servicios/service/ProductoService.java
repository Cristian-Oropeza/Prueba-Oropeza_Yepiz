package com.proyecto.servicios.service;

import com.proyecto.servicios.model.producto.ProductoResponse;
import com.proyecto.servicios.model.producto.SincronizacionProductosResponse;

import java.util.List;

public interface ProductoService {

    SincronizacionProductosResponse sincronizarProductos();

    List<ProductoResponse> consultarProductos(String servicio);
}
