package com.proyecto.servicios.scheduler;

import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.model.producto.SincronizacionProductosResponse;
import com.proyecto.servicios.service.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductoSyncScheduler {

    private final ProductoService productoService;

    public ProductoSyncScheduler(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Scheduled(cron = "${gestopago.product.sync-cron}", zone = "${gestopago.product.sync-zone}")
    public void sincronizarCatalogo() {
        log.info("Inicio sincronizacion programada del catalogo GestoPago");
        try {
            SincronizacionProductosResponse response = productoService.sincronizarProductos();
            log.info("Fin sincronizacion programada del catalogo GestoPago: {} productos",
                    response.getTotalProductos());
        } catch (GestoPagoIntegrationException e) {
            log.error("Sincronizacion programada del catalogo GestoPago fallida: codigo={}, detalle={}",
                    e.getErrorCode().getCode(), e.getMessage());
        }
    }
}
