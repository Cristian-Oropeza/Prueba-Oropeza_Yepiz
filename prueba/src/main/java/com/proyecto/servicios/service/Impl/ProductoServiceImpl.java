package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.document.gestopago.ProductoDocument;
import com.proyecto.servicios.exception.GestoPagoErrorCode;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.integration.GestoPagoProductIntegration;
import com.proyecto.servicios.mapper.ProductoMapper;
import com.proyecto.servicios.model.gestopago.product.GestoPagoProduct;
import com.proyecto.servicios.model.producto.ProductoResponse;
import com.proyecto.servicios.model.producto.SincronizacionProductosResponse;
import com.proyecto.servicios.repositorys.mongo.ProductoRepository;
import com.proyecto.servicios.service.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ProductoServiceImpl implements ProductoService {

    private static final int CODIGO_EXITO = 0;
    private static final String MENSAJE_EXITO = "Catalogo de productos sincronizado correctamente";

    private final GestoPagoProductIntegration productIntegration;
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final Clock clock;

    public ProductoServiceImpl(GestoPagoProductIntegration productIntegration,
                               ProductoRepository productoRepository,
                               ProductoMapper productoMapper,
                               Clock clock) {
        this.productIntegration = productIntegration;
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.clock = clock;
    }

    @Override
    public SincronizacionProductosResponse sincronizarProductos() {
        List<GestoPagoProduct> productos = descartarSinIdentificador(productIntegration.obtenerProductos());
        validarCatalogo(productos);

        LocalDateTime fechaSincronizacion = ahora();
        productoRepository.saveAll(toDocuments(productos, fechaSincronizacion));
        long eliminados = productoRepository.deleteByFechaSincronizacionBefore(fechaSincronizacion);

        log.info("Catalogo GestoPago sincronizado: {} productos guardados, {} productos obsoletos eliminados",
                productos.size(), eliminados);
        return buildResponse(productos.size(), eliminados, fechaSincronizacion);
    }

    @Override
    public List<ProductoResponse> consultarProductos(String servicio) {
        List<ProductoDocument> documentos = StringUtils.isBlank(servicio)
                ? productoRepository.findAllByOrderByServicioAscProductoAsc()
                : productoRepository.findByServicioIgnoreCaseOrderByProductoAsc(servicio.trim());
        return productoMapper.toResponses(documentos);
    }

    private List<GestoPagoProduct> descartarSinIdentificador(List<GestoPagoProduct> productos) {
        List<GestoPagoProduct> validos = productos.stream()
                .filter(producto -> Objects.nonNull(producto.getIdProducto()))
                .toList();
        int descartados = productos.size() - validos.size();
        if (descartados > 0) {
            log.warn("Se descartaron {} productos de GestoPago sin idProducto", descartados);
        }
        return validos;
    }

    private void validarCatalogo(List<GestoPagoProduct> productos) {
        if (productos.isEmpty()) {
            throw new GestoPagoIntegrationException(GestoPagoErrorCode.EMPTY_CATALOG);
        }
    }

    private List<ProductoDocument> toDocuments(List<GestoPagoProduct> productos, LocalDateTime fechaSincronizacion) {
        return productos.stream()
                .map(producto -> productoMapper.toDocument(producto, fechaSincronizacion))
                .toList();
    }

    /**
     * MongoDB guarda las fechas con precision de milisegundos; se trunca para que la
     * comparacion contra los documentos recien guardados sea exacta.
     */
    private LocalDateTime ahora() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
    }

    private SincronizacionProductosResponse buildResponse(int totalProductos, long eliminados,
                                                          LocalDateTime fechaSincronizacion) {
        SincronizacionProductosResponse response = new SincronizacionProductosResponse();
        response.setCodigo(CODIGO_EXITO);
        response.setMensaje(MENSAJE_EXITO);
        response.setTotalProductos(totalProductos);
        response.setProductosEliminados(eliminados);
        response.setFechaSincronizacion(fechaSincronizacion);
        return response;
    }
}
