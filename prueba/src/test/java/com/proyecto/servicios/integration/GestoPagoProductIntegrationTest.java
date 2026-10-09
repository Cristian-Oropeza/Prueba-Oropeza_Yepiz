package com.proyecto.servicios.integration;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.exception.GestoPagoErrorCode;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.model.gestopago.product.GestoPagoProduct;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.proyecto.servicios.GestoPagoTestFixtures.readResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestoPagoProductIntegrationTest {

    @Mock
    private GestoPagoProductClient productClient;

    private GestoPagoProductIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new GestoPagoProductIntegration(productClient, new GestoPagoProductXmlParser());
    }

    @Test
    void obtenerProductos_respuestaExitosa_regresaProductos() {
        when(productClient.getProductList()).thenReturn(readResource("/gestopago/product-list-success.xml"));

        List<GestoPagoProduct> productos = integration.obtenerProductos();

        assertThat(productos).extracting(GestoPagoProduct::getIdProducto).containsExactly(14302, 19377, 487);
    }

    @Test
    void obtenerProductos_codigoDistintoDeExito_lanzaBusinessError() {
        when(productClient.getProductList()).thenReturn(readResource("/gestopago/product-list-error-code.xml"));

        assertThatThrownBy(() -> integration.obtenerProductos())
                .isInstanceOf(GestoPagoIntegrationException.class)
                .hasMessageContaining("codigo=02")
                .extracting("errorCode")
                .isEqualTo(GestoPagoErrorCode.BUSINESS_ERROR);
    }

    @Test
    void obtenerProductos_respuestaSinMensaje_lanzaInvalidResponse() {
        when(productClient.getProductList())
                .thenReturn("<RESPONSE><PRODUCTOS/></RESPONSE>".getBytes(StandardCharsets.UTF_8));

        assertErrorCode(GestoPagoErrorCode.INVALID_RESPONSE);
    }

    @Test
    void obtenerProductos_errorDeAutenticacionDelDecoder_sePropagaSinCambios() {
        when(productClient.getProductList())
                .thenThrow(new GestoPagoIntegrationException(GestoPagoErrorCode.AUTHENTICATION_ERROR, "HTTP 401"));

        assertErrorCode(GestoPagoErrorCode.AUTHENTICATION_ERROR);
    }

    @Test
    void obtenerProductos_timeout_lanzaTimeout() {
        when(productClient.getProductList()).thenThrow(retryable(new SocketTimeoutException("Read timed out")));

        assertErrorCode(GestoPagoErrorCode.TIMEOUT);
    }

    @Test
    void obtenerProductos_conexionRechazada_lanzaCommunicationError() {
        when(productClient.getProductList()).thenThrow(retryable(new ConnectException("Connection refused")));

        assertErrorCode(GestoPagoErrorCode.COMMUNICATION_ERROR);
    }

    private void assertErrorCode(GestoPagoErrorCode expected) {
        assertThatThrownBy(() -> integration.obtenerProductos())
                .isInstanceOf(GestoPagoIntegrationException.class)
                .extracting("errorCode")
                .isEqualTo(expected);
    }

    private static RetryableException retryable(IOException cause) {
        Request request = Request.create(Request.HttpMethod.GET,
                "https://gestopago.test/sistema/service/getProductList.do",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return new RetryableException(-1, cause.getMessage(), Request.HttpMethod.GET, cause, (Long) null, request);
    }
}
