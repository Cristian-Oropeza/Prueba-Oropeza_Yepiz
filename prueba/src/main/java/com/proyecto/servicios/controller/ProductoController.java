package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.producto.ProductoResponse;
import com.proyecto.servicios.model.producto.SincronizacionProductosResponse;
import com.proyecto.servicios.service.ProductoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductoResponse>> consultarProductos(
            @RequestParam(required = false) String servicio) {
        return ResponseEntity.ok(productoService.consultarProductos(servicio));
    }

    @PostMapping(value = "/sincronizacion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SincronizacionProductosResponse> sincronizarProductos() {
        return ResponseEntity.ok(productoService.sincronizarProductos());
    }
}
