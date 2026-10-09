package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.document.gestopago.ProductoDocument;
import com.proyecto.servicios.exception.GestoPagoErrorCode;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.integration.GestoPagoProductIntegration;
import com.proyecto.servicios.mapper.ProductoMapper;
import com.proyecto.servicios.model.producto.ProductoResponse;
import com.proyecto.servicios.model.producto.SincronizacionProductosResponse;
import com.proyecto.servicios.repositorys.mongo.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.proyecto.servicios.GestoPagoTestFixtures.product;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final Instant INSTANTE_FIJO = Instant.parse("2026-09-23T12:00:00.123456789Z");
    private static final LocalDateTime FECHA_SINCRONIZACION =
            LocalDateTime.ofInstant(Instant.parse("2026-09-23T12:00:00.123Z"), ZONA);

    @Mock
    private GestoPagoProductIntegration productIntegration;

    @Mock
    private ProductoRepository productoRepository;

    @Captor
    private ArgumentCaptor<List<ProductoDocument>> documentosCaptor;

    private final ProductoMapper productoMapper = Mappers.getMapper(ProductoMapper.class);

    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(INSTANTE_FIJO, ZONA);
        productoService = new ProductoServiceImpl(productIntegration, productoRepository, productoMapper, clock);
    }

    @Test
    void sincronizarProductos_guardaCatalogoYEliminaProductosObsoletos() {
        when(productIntegration.obtenerProductos()).thenReturn(List.of(
                product(14302, "ABIB", "ABIB 100", "100.0"),
                product(487, "Cinepolis", "Cinepolis 2 boletos", "100.0")));
        when(productoRepository.deleteByFechaSincronizacionBefore(FECHA_SINCRONIZACION)).thenReturn(3L);

        SincronizacionProductosResponse response = productoService.sincronizarProductos();

        verify(productoRepository).saveAll(documentosCaptor.capture());
        List<ProductoDocument> guardados = documentosCaptor.getValue();
        assertThat(guardados).extracting(ProductoDocument::getIdProducto).containsExactly(14302, 487);
        assertThat(guardados).allSatisfy(documento ->
                assertThat(documento.getFechaSincronizacion()).isEqualTo(FECHA_SINCRONIZACION));
        assertThat(guardados.get(0).getPrecio()).isEqualByComparingTo(new BigDecimal("100.0"));

        assertThat(response.getCodigo()).isZero();
        assertThat(response.getTotalProductos()).isEqualTo(2);
        assertThat(response.getProductosEliminados()).isEqualTo(3L);
        assertThat(response.getFechaSincronizacion()).isEqualTo(FECHA_SINCRONIZACION);
    }

    @Test
    void sincronizarProductos_truncaLaFechaAMilisegundosComoLaGuardaMongo() {
        when(productIntegration.obtenerProductos()).thenReturn(List.of(product(1, "Telcel", "Recarga $10", "10.0")));

        SincronizacionProductosResponse response = productoService.sincronizarProductos();

        assertThat(response.getFechaSincronizacion().getNano()).isEqualTo(123_000_000);
        verify(productoRepository).deleteByFechaSincronizacionBefore(FECHA_SINCRONIZACION);
    }

    @Test
    void sincronizarProductos_descartaProductosSinIdentificador() {
        when(productIntegration.obtenerProductos()).thenReturn(List.of(
                product(14302, "ABIB", "ABIB 100", "100.0"),
                product(null, "Sin id", "Producto invalido", "10.0")));

        SincronizacionProductosResponse response = productoService.sincronizarProductos();

        verify(productoRepository).saveAll(documentosCaptor.capture());
        assertThat(documentosCaptor.getValue()).extracting(ProductoDocument::getIdProducto).containsExactly(14302);
        assertThat(response.getTotalProductos()).isEqualTo(1);
    }

    @Test
    void sincronizarProductos_catalogoVacio_lanzaErrorYNoModificaMongo() {
        when(productIntegration.obtenerProductos()).thenReturn(List.of());

        assertThatThrownBy(() -> productoService.sincronizarProductos())
                .isInstanceOf(GestoPagoIntegrationException.class)
                .extracting("errorCode")
                .isEqualTo(GestoPagoErrorCode.EMPTY_CATALOG);

        verifyNoInteractions(productoRepository);
    }

    @ParameterizedTest
    @EnumSource(value = GestoPagoErrorCode.class, names = {
            "AUTHENTICATION_ERROR", "TIMEOUT", "COMMUNICATION_ERROR",
            "SERVICE_UNAVAILABLE", "CLIENT_ERROR", "INVALID_RESPONSE", "BUSINESS_ERROR"})
    void sincronizarProductos_errorDeIntegracion_sePropagaYNoModificaMongo(GestoPagoErrorCode errorCode) {
        when(productIntegration.obtenerProductos()).thenThrow(new GestoPagoIntegrationException(errorCode));

        assertThatThrownBy(() -> productoService.sincronizarProductos())
                .isInstanceOf(GestoPagoIntegrationException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);

        verifyNoInteractions(productoRepository);
    }

    @Test
    void consultarProductos_sinFiltro_regresaTodoElCatalogo() {
        when(productoRepository.findAllByOrderByServicioAscProductoAsc()).thenReturn(List.of(documento(14302, "ABIB")));

        List<ProductoResponse> productos = productoService.consultarProductos(null);

        assertThat(productos).extracting(ProductoResponse::getIdProducto).containsExactly(14302);
    }

    @Test
    void consultarProductos_conServicio_filtraPorServicio() {
        when(productoRepository.findByServicioIgnoreCaseOrderByProductoAsc("Telcel"))
                .thenReturn(List.of(documento(406, "Telcel")));

        List<ProductoResponse> productos = productoService.consultarProductos("  Telcel ");

        assertThat(productos).extracting(ProductoResponse::getServicio).containsExactly("Telcel");
        verify(productoRepository).findByServicioIgnoreCaseOrderByProductoAsc(any());
    }

    private static ProductoDocument documento(Integer idProducto, String servicio) {
        ProductoDocument documento = new ProductoDocument();
        documento.setIdProducto(idProducto);
        documento.setServicio(servicio);
        return documento;
    }
}
