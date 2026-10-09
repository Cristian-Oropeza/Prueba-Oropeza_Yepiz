package com.proyecto.servicios.integration;

import com.proyecto.servicios.exception.GestoPagoErrorCode;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.model.gestopago.product.GestoPagoProduct;
import com.proyecto.servicios.model.gestopago.product.GestoPagoProductListResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static com.proyecto.servicios.GestoPagoTestFixtures.readResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GestoPagoProductXmlParserTest {

    private final GestoPagoProductXmlParser parser = new GestoPagoProductXmlParser();

    @Test
    void parse_respuestaExitosa_mapeaMensajeYProductos() {
        GestoPagoProductListResponse response = parser.parse(readResource("/gestopago/product-list-success.xml"));

        assertThat(response.getMensaje().getCodigo()).isEqualTo("01");
        assertThat(response.getMensaje().isExitoso()).isTrue();
        assertThat(response.getProductos()).hasSize(3);

        GestoPagoProduct kaspersky = response.getProductos().get(1);
        assertThat(kaspersky.getIdProducto()).isEqualTo(19377);
        assertThat(kaspersky.getIdServicio()).isEqualTo(3246);
        assertThat(kaspersky.getIdCatTipoServicio()).isEqualTo(10);
        assertThat(kaspersky.getTipoFront()).isEqualTo(1);
        assertThat(kaspersky.getServicio()).isEqualTo("Kaspersky Cloud Password Manager");
        assertThat(kaspersky.getProducto()).isEqualTo("1 Usuario, 1 Ano");
        assertThat(kaspersky.getPrecio()).isEqualByComparingTo(new BigDecimal("368.76"));
        assertThat(kaspersky.getHasDigitoVerificador()).isFalse();
        assertThat(kaspersky.getShowAyuda()).isFalse();
        assertThat(kaspersky.getTipoReferencia()).isEqualTo("a");
    }

    @Test
    void parse_legend_quitaEspaciosDeLaOrillaYConservaAcentosYSaltosDeLinea() {
        GestoPagoProductListResponse response = parser.parse(readResource("/gestopago/product-list-success.xml"));

        String legend = response.getProductos().get(2).getLegend();
        assertThat(legend)
                .startsWith("¡Gracias por tu compra!")
                .contains("\n")
                .contains("“Agregar folios y promociones”")
                .endsWith("codigo de tu folio.");
    }

    @Test
    void parse_codigoDeError_regresaMensajeNoExitoso() {
        GestoPagoProductListResponse response = parser.parse(readResource("/gestopago/product-list-error-code.xml"));

        assertThat(response.getMensaje().isExitoso()).isFalse();
        assertThat(response.getProductos()).isEmpty();
    }

    @Test
    void parse_respuestaJson_lanzaInvalidResponse() {
        assertInvalidResponse(readResource("/gestopago/auth-error.json"));
    }

    @Test
    void parse_xmlMalFormado_lanzaInvalidResponse() {
        assertInvalidResponse("<RESPONSE><MENSAJE>".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parse_cuerpoVacioONulo_lanzaInvalidResponse() {
        assertInvalidResponse(new byte[0]);
        assertInvalidResponse(null);
    }

    @Test
    void parse_xmlConEntidadExterna_seRechazaParaEvitarXxe() {
        String xxe = """
                <?xml version="1.0"?>
                <!DOCTYPE RESPONSE [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <RESPONSE><MENSAJE><CODIGO>&xxe;</CODIGO></MENSAJE></RESPONSE>
                """;
        assertInvalidResponse(xxe.getBytes(StandardCharsets.UTF_8));
    }

    private void assertInvalidResponse(byte[] body) {
        assertThatThrownBy(() -> parser.parse(body))
                .isInstanceOf(GestoPagoIntegrationException.class)
                .extracting("errorCode")
                .isEqualTo(GestoPagoErrorCode.INVALID_RESPONSE);
    }
}
