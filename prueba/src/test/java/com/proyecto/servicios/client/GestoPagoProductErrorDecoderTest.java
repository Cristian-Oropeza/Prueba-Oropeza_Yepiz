package com.proyecto.servicios.client;

import com.proyecto.servicios.exception.GestoPagoErrorCode;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GestoPagoProductErrorDecoderTest {

    private final GestoPagoProductErrorDecoder decoder = new GestoPagoProductErrorDecoder();

    @ParameterizedTest
    @CsvSource({
            "401, AUTHENTICATION_ERROR",
            "403, AUTHENTICATION_ERROR",
            "400, CLIENT_ERROR",
            "404, CLIENT_ERROR",
            "500, SERVICE_UNAVAILABLE",
            "503, SERVICE_UNAVAILABLE"
    })
    void decode_mapeaEstatusHttpACodigoDeError(int status, GestoPagoErrorCode expected) {
        Exception exception = decoder.decode("GestoPagoProductClient#getProductList()", response(status));

        assertThat(exception).isInstanceOf(GestoPagoIntegrationException.class);
        assertThat(((GestoPagoIntegrationException) exception).getErrorCode()).isEqualTo(expected);
        assertThat(exception.getMessage()).contains("HTTP " + status).doesNotContain("Bearer");
    }

    private static Response response(int status) {
        Request request = Request.create(Request.HttpMethod.GET,
                "https://gestopago.test/sistema/service/getProductList.do",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return Response.builder()
                .status(status)
                .reason("test")
                .request(request)
                .headers(Map.of())
                .build();
    }
}
