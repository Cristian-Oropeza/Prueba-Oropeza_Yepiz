package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.ErrorResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GestoPagoExceptionHandlerTest {

    private final GestoPagoExceptionHandler handler = new GestoPagoExceptionHandler();

    @ParameterizedTest
    @CsvSource({
            "TIMEOUT, 504",
            "SERVICE_UNAVAILABLE, 503",
            "AUTHENTICATION_ERROR, 502",
            "BUSINESS_ERROR, 502",
            "EMPTY_CATALOG, 502"
    })
    void handleIntegrationException_regresaEstatusYCodigoSinDetalleInterno(GestoPagoErrorCode errorCode, int status) {
        GestoPagoIntegrationException exception =
                new GestoPagoIntegrationException(errorCode, "detalle interno que no debe exponerse");

        ResponseEntity<ErrorResponse> response = handler.handleIntegrationException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCodigo()).isEqualTo(errorCode.getCode());
        assertThat(response.getBody().getMensaje()).isEqualTo(errorCode.getMessage());
    }
}
